package com.solo.blogger.service;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.NotificationResponse;
import com.solo.blogger.dto.apiResponse.PostCreatedEvent;
import com.solo.blogger.entity.Notification;
import com.solo.blogger.repository.NotificationRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public ApiResponseDto<?>getNotifications(Long userId, int page, int limit) {
        if(userId == null)  throw new RuntimeException("User id is null");
        Page<Notification> notifications=notificationRepository.findAllByUserId(userId, PageRequest.of(page,limit));
        NotificationResponse userNotifications = NotificationResponse.builder().notifications(notifications.getContent()).unreadCount(notifications.getTotalElements()).build();
        return ApiResponseDto.success(userNotifications);
    }

    public ApiResponseDto<?>getUnreadCount(Long userId) {
        if(userId == null)  throw new RuntimeException("User id is null");
        Long count=notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        NotificationResponse userNotifications = NotificationResponse.builder().notifications(null).unreadCount(count).build();
        return ApiResponseDto.success(userNotifications);
    }

    public void notificationEntry(Long userId,Long postId,String title,String authorName) {
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


}
