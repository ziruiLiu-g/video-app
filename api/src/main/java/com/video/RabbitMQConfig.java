//package com.video;
//
//import org.springframework.amqp.core.*;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//    /**---------------------------------
//     * define exchanger
//     * define queue
//     * create exchanger
//     * create queue
//     * bind exchanger and queue
//     * ---------------------------------
//     */
//    
//    public static final String EXCHANGE_MSG = "exchange_msg";
//
//    public static final String QUEUE_SYS_MSG = "queue_sys_msg";
//    
//    @Bean(EXCHANGE_MSG)
//    public Exchange exchange() {
//        return ExchangeBuilder                      // build exchange
//                .topicExchange(EXCHANGE_MSG)        // topic
//                .durable(true)                      // my exist after reboot
//                .build();
//    }
//
//    @Bean(QUEUE_SYS_MSG)
//    public Queue queue() {
//        return new Queue(QUEUE_SYS_MSG);
//    }
//
//    @Bean
//    public Binding binding(@Qualifier(EXCHANGE_MSG) Exchange exchange, 
//                           @Qualifier(QUEUE_SYS_MSG) Queue queue) {
//        return BindingBuilder
//                .bind(queue)
//                .to(exchange)
//                .with("sys.msg.*")         // exchange rule
//                .noargs();
//    }
//}
