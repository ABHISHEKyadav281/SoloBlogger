package com.solo.blogger.kafka;

import com.solo.blogger.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PostEventConsumer {
    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "post-created", groupId = "email-service")
    public void informSubscribersForNewPost(String message) {
        System.out.println("consumer found one notification  : " + message);

        String to = "yyadavabhishek9@gmail.com";
        String subject = "New blog post alert!";
        String text = message;

        emailService.sendNewPostNotification(to, subject, text);
    }
}
