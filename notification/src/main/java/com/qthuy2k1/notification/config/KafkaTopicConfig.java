package com.qthuy2k1.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public NewTopic sendEmailUserCreatedTopic() {
        return TopicBuilder.name("topic1").build();
    }
}
