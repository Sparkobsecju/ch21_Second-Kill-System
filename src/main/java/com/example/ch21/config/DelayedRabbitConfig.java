package com.example.ch21.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedRabbitConfig {
    public static final String DELAY_EXCHANGE_NAME = "delay_exchange";
    public static final String DELAY_QUEUE_NAME = "delay_queue";

    @Bean
    public Queue delayQueue() {
        // 持久化、非獨占的、非自動刪除的佇列
        return new Queue(DELAY_QUEUE_NAME);
    }
    // 定義 direct 類別的延遲交換器
    @Bean
    DirectExchange delayExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        DirectExchange directExchange = new DirectExchange(DELAY_EXCHANGE_NAME, true, false, args);
        directExchange.setDelayed(true);
        return directExchange;
    }

    // 綁定延遲佇列嶼交換機
    @Bean
    public Binding delayBind() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(DELAY_QUEUE_NAME);
    }
}
