package com.qthuy2k1.notification;

import com.qthuy2k1.notification.dto.OrderItemPlaced;
import com.qthuy2k1.notification.dto.OrderPlaced;
import com.qthuy2k1.notification.dto.UserCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListeners {
    private final MailSender mailSender;

    @KafkaListener(topics = "topic1", groupId = "group1")
    void listener(String data) {
        System.out.println("Sending");
        System.out.println("Listener received: " + data + "🎉");
    }

    @KafkaListener(topics = "create-user", groupId = "create-user-group")
    public void sendUserCreatedNotificationMailListener(UserCreated user) {
        log.info("Email received: " + user.getToEmail());

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("qthuy2609@gmail.com");
        message.setTo("qthuy2609@gmail.com");
        message.setText("You created an user account in product management application");
        message.setSubject("User created");

//        mailSender.send(message);

        log.info("Mail sent successfully...");
    }

    @KafkaListener(topics = "create-order", groupId = "create-order-group")
    public void sendOrderCreatedNotificationMailListener(OrderPlaced order) {
        System.out.println(order.getStatus());
        System.out.println(order.getTotalAmount());
        System.out.println(order.getCreatedAt());
        System.out.println(order.getUpdatedAt());
        for (OrderItemPlaced orderItem : order.getOrderItems()) {
            System.out.println(orderItem.getPrice());
            System.out.println(orderItem.getProductName());
            System.out.println(orderItem.getQuantity());
        }
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("qthuy2609@gmail.com");
        message.setTo("qthuy2609@gmail.com");

        String body = setBody(order);
        message.setText(body);
        message.setSubject("Order created");

//        mailSender.send(message);

        log.info("Mail sent successfully...");
    }

    private String setBody(OrderPlaced order) {
        String body = "";
        body = body.concat("You created an order in product management application");
        body = body.concat(String.format("%nOrder Status: %s%n", order.getStatus()));
        body = body.concat(String.format("Total Amount: $%s%n", order.getTotalAmount()));
        body = body.concat(String.format("Created At: %s%n", order.getCreatedAt()));
        body = body.concat(String.format("Updated At: %s%n", order.getUpdatedAt()));
        body = body.concat(String.format("Your order detail is listed below:%n%n"));
        body = body.concat(String.format("%s%30s%30s%n", "Product Name", "Price", "Quantity"));
        for (OrderItemPlaced orderItem : order.getOrderItems()) {
            body = body.concat(String.format("%s%30s%30s%n", orderItem.getProductName(), orderItem.getPrice(), orderItem.getQuantity()));
        }
        return body;
    }
}
