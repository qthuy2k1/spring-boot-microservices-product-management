package com.qthuy2k1.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaListeners {
    private final MailSender mailSender;

    @Value("${spring.mail.password}")
    private String password;

    @KafkaListener(topics = "topic1", groupId = "group1")
    void listener(String data) {
        System.out.println("Sending");
        System.out.println("Listener received: " + data + "🎉");
    }

    @KafkaListener(topics = "create-user", groupId = "create-user-group")
    public void sendCreatedUserNotificationMailListener(String toEmail) {
        System.out.println("Email received: " + toEmail);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("qthuy2609@gmail.com");
        message.setTo("qthuy2609@gmail.com");
        message.setText("You created an user account in product management application");
        message.setSubject("User created");

        mailSender.send(message);

        System.out.println("Mail sent successfully...");
    }
}
