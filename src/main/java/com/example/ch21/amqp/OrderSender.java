package com.example.ch21.amqp;

import com.example.ch21.persistence.entity.SeckillOrderPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.example.ch21.config.DelayedRabbitConfig.DELAY_EXCHANGE_NAME;
import static com.example.ch21.config.DelayedRabbitConfig.DELAY_QUEUE_NAME;

@Component
public class OrderSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Integer DELAY_TIME = 10 * 60 * 1000; // 延遲 10 分鐘
    @Autowired
    private AmqpTemplate rabbitTemplate;

    /**
     * 發送延遲訊息，客戶過期未支付，取消訂單
     * @param orderPK 限時搶購訂單主鍵
     */
    public void sendDelayMsg(SeckillOrderPK orderPK) {
        rabbitTemplate.convertAndSend(
                DELAY_EXCHANGE_NAME, DELAY_QUEUE_NAME, orderPK, message -> {
                    message.getMessageProperties()
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 指定訊息延遲的時長為 10 分鐘，以毫秒為單位
                    message.getMessageProperties().setDelay(DELAY_TIME);
                    return message;
                });

        logger.info("當前時間是：" + new Date());
        logger.info(" [x] Sent '" + orderPK + "'");
    }
}


