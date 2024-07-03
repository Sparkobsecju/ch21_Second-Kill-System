package com.example.ch21.amqp;

import com.example.ch21.config.RedisConfig;
import com.example.ch21.persistence.entity.SeckillItem;
import com.example.ch21.persistence.entity.SeckillOrder;
import com.example.ch21.persistence.entity.SeckillOrderPK;
import com.example.ch21.exception.OrderInvalidationException;
import com.example.ch21.persistence.repository.SeckillItemRepository;
import com.example.ch21.persistence.repository.SeckillOrderRepository;
import com.rabbitmq.client.Channel;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.example.ch21.config.DelayedRabbitConfig.DELAY_QUEUE_NAME;

@Component
public class OrderComsumer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillOrderRepository seckillOrderRepository;
    @Autowired
    private SeckillItemRepository seckillItemRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @RabbitListener(queues = DELAY_QUEUE_NAME, ackMode = "MANUAL")
    public void process(SeckillOrderPK orderPK, Message message, Channel channel)
        throws Exception {
        SeckillOrder order = seckillOrderRepository.getById(orderPK);
        // 如果訂單未支付，則取消訂單，增加商品庫存
        if(order.getState() == 0) {
            logger.info("訂單[%d]支付逾時%n", order.getItemId());
            logger.info("開始取消訂單......");

            // 將訂單設定為無效訂單
            order.setState(-1);
            seckillOrderRepository.save(order);
            // 恢復庫存
            SeckillItem item = (SeckillItem) redisTemplate.opsForHash().get(
                    RedisConfig.ITEM_KEY, orderPK.getItemId());
            item.setInventory(item.getInventory() + 1);
            redisTemplate.opsForHash().put(RedisConfig.ITEM_KEY, orderPK.getItemId()
            ,item);
            seckillItemRepository.save(item);
            logger.info("訂單取消完畢");
        }
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),
                    false);
        }
    }

    /**
     * 訂單支付。將訂單狀態設定為已支付
     * @param id 商品 ID
     * @param mobile 使用者手機號
     */
    public void pay(Integer id, String mobile) {
        SeckillOrder order = seckillOrderRepository.getById(new SeckillOrderPK(id, mobile));
        int state = order.getState();
        if (state == 0) { // 如果是未支付，則設定為已支付
            order.setState(1);
            seckillOrderRepository.save(order);
        } else if (state == -1) { // 如果訂單已經故障，則拋出例外
            throw new OrderInvalidationException();
        }
    }


}
