package com.qthuy2k1.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public NewTopic sendEmailUserCreatedTopic() {
        return TopicBuilder.name("create-user").build();
    }

    public NewTopic sendEmailOrderCreatedTopic() {
        return TopicBuilder.name("create-order").build();
    }
}
