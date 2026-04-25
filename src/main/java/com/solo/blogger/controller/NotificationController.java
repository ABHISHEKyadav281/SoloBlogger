package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(notificationService.getNotifications(userId, page, limit)));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(notificationService.getUnreadCount(userId)));
    }


    @PostMapping("/read")
    public ResponseEntity<?> readNotifications(Long notificationId) {
        notificationService.readNotification(notificationId);
        return ResponseEntity.status(200).build();
    }
}