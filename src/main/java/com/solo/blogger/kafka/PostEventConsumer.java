package com.solo.blogger.kafka;

import com.solo.blogger.dto.apiResponse.PostCreatedEvent;
import com.solo.blogger.entity.Notification;
import com.solo.blogger.repository.NotificationRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import com.solo.blogger.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostEventConsumer {
    @Autowired
    private EmailService emailService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "post-created", groupId = "email-service")
    public void informSubscribersForNewPost(PostCreatedEvent event) {
        if (event.getAuthorId() == null) throw new RuntimeException("User id is null");
        List<Long> followerIds = subscriptionRepository.findByBloggerId(event.getAuthorId());
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        List<Notification> notifications = followerIds.stream()
                .map(followerId -> {
                    Notification n = new Notification();
                    n.setUserId(followerId);
                    n.setPostId(event.getPostId());
                    n.setMessage(event.getAuthorName() + " published a new post: " + event.getTitle());
                    n.setRead(false);
                    n.setCreatedAt(LocalDateTime.now());
                    return n;
                })
                .toList();

        notificationRepository.saveAll(notifications);

//        String to = "yyadavabhishek9@gmail.com";
//        String subject = "New blog post alert!";
//        String text = event.getAuthorName() + " published: " + event.getTitle();
//        emailService.sendNewPostNotification("yyadavabhishek9@gmail.com", subject, text);
    }
}
