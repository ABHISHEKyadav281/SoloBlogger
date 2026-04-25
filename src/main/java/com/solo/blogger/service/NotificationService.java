package com.solo.blogger.service;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.NotificationResponse;
import com.solo.blogger.dto.apiResponse.PostCreatedEvent;
import com.solo.blogger.entity.Notification;
import com.solo.blogger.repository.NotificationRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public NotificationResponse getNotifications(Long userId, int page, int limit) {
        if (userId == null) throw new RuntimeException("User id is null");
        Page<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, limit));
        return NotificationResponse.builder()
                .notifications(notifications.getContent())
                .unreadCount(notifications
                        .getTotalElements()).build();
    }

    public NotificationResponse getUnreadCount(Long userId) {
        if (userId == null) throw new RuntimeException("User id is null");
        Long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationResponse.builder().notifications(null).unreadCount(count).build();
    }

    public void notificationEntry(Long userId, Long postId, String title, String authorName) {
        if (userId == null) throw new RuntimeException("User id is null");
        List<Long> followerIds = subscriptionRepository.findByBloggerId(userId);
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        List<Notification> notifications = followerIds.stream()
                .map(followerId -> {
                    Notification n = new Notification();
                    n.setUserId(followerId);
                    n.setPostId(postId);
                    n.setMessage(authorName + " published a new post: " + title);
                    n.setRead(false);
                    n.setCreatedAt(LocalDateTime.now());
                    return n;
                })
                .toList();

        notificationRepository.saveAll(notifications);

    }

    public void readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

}
