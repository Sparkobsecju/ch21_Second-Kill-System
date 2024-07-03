package com.example.ch21.service;

import com.example.ch21.amqp.OrderSender;
import com.example.ch21.config.RedisConfig;
import com.example.ch21.persistence.entity.SeckillItem;
import com.example.ch21.persistence.entity.SeckillOrder;
import com.example.ch21.persistence.entity.SeckillOrderPK;
import com.example.ch21.exception.*;
import com.example.ch21.persistence.repository.SeckillItemRepository;
import com.example.ch21.persistence.repository.SeckillOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeckillService {
    @Autowired
    private SeckillItemRepository seckillItemRepository;
    @Autowired
    private SeckillOrderRepository seckillOrderRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderSender orderSender;

    /**
     * 獲取所有限時搶購商品，如果商品資訊在 Redis 中部存在，則在第一次從資料庫中獲取商品資訊時，
     * 將該資訊儲存到 Redis 中。
     * @return
     */
    public List<SeckillItem> getAllItem() {
        List<SeckillItem> items = redisTemplate.opsForHash().values(RedisConfig.ITEM_KEY);
        if (items == null || items.size() == 0) {
            items = seckillItemRepository.findAll();
            for (SeckillItem item: items) {
                redisTemplate.opsForHash().put(RedisConfig.ITEM_KEY, item.getId(), item);
            }
        }
        return items;
    }

    /**
     * 獲取限時搶購商品資訊。判斷使用者訂單是否已經存在，如果已經存在，則判斷訂單狀態；如果不存在，
     * 則傳回使用者選擇的限時搶購商品。
     * @param id 商品 ID
     * @param mobile 使用者手機號
     * @return 商品物件
     * @throws SeckillException
     */
    public SeckillItem getItemById(Integer id, String mobile) throws SeckillException {
        SeckillOrderPK pk = new SeckillOrderPK(id, mobile);
        Optional<SeckillOrder> optionalOrder = seckillOrderRepository.findById(pk);
        if (!optionalOrder.isEmpty()) {
            SeckillOrder order = optionalOrder.get();

            if (order.getState() == 1) { // 訂單已支付，重複限時搶購
                throw new RepeatSeckillException();
            } else if (order.getState() == 0) {
                throw new UnpaidException(); // 以限時搶購，但還未支付
            } else { // 訂單已經故障
                throw new OrderInvalidationException();
            }
        }

        SeckillItem item = (SeckillItem) redisTemplate.opsForHash().get(RedisConfig.ITEM_KEY, id);
        if (item == null) {
            item = seckillItemRepository.getById(id);
        }
        return item;
    }

    /**
     * 執行限時搶購邏輯。
     * @param id 商品 ID
     * @param mobile 使用者手機號
     * @throws InsufficientInventoryException
     */
    public void execSeckill(Integer id, String mobile) throws InsufficientInventoryException {
        SeckillItem item = (SeckillItem) redisTemplate.opsForHash().get(RedisConfig.ITEM_KEY, id);
        Integer inventory = item.getInventory();
        // 如果庫存不足，則拋出例外，交給 Web 層進行處理
        if (inventory <= 0) {
            throw new InsufficientInventoryException();
        }
        // 將庫存遞減 1
        item.setInventory(item.getInventory() - 1);
        redisTemplate.opsForHash().put(RedisConfig.ITEM_KEY, id, item);
        seckillItemRepository.save(item);

        // 儲存訂單
        SeckillOrder order = new SeckillOrder();
        order.setItemId(id);
        order.setMobile(mobile);
        order.setMoney(item.getSeckillPrice());
        order.setState(0);
        seckillOrderRepository.save(order);

        // 發送延遲訂單處理訊息
        orderSender.sendDelayMsg(new SeckillOrderPK(id, mobile));
    }

    public void pay(Integer id, String mobile) {
        SeckillOrder order = seckillOrderRepository.getById(new SeckillOrderPK(id, mobile));
        int state = order.getState();
        if (state == 0) {
            order.setState(1);
            seckillOrderRepository.save(order);
        } else if (state == -1) {
            throw new OrderInvalidationException();
        }
    }
}

















