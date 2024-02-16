package com.qthuy2k1.user;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {
    @KafkaListener(topics = "create-user", groupId = "createUserGroup")
    void listener(String data) {
        System.out.println("Listener received: " + data + "🎉");
    }
}
