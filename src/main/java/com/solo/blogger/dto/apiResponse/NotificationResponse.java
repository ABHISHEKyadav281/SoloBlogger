package com.solo.blogger.dto.apiResponse;

import com.solo.blogger.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class NotificationResponse {
    private List<Notification> notifications;
    private long unreadCount;
    private int totalPages;
}
