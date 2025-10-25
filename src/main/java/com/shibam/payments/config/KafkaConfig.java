package com.shibam.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic paymentNotificationsTopic() {
        return TopicBuilder.name("payment-notifications")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic refundNotificationsTopic() {
        return TopicBuilder.name("refund-notifications")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name("fraud-alerts")
                .partitions(2)
                .replicas(1)
                .build();
    }
}