package com.qthuy2k1.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public NewTopic sendEmailUserCreatedTopic() {
        return TopicBuilder.name("create-user").build();
    }
}
