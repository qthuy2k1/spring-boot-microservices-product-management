package com.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public NewTopic sendEmailOrderCreatedTopic() {
        return TopicBuilder.name("create-order").build();
    }
}
