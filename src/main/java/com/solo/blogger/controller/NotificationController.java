package com.solo.blogger.controller;

import com.solo.blogger.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestAttribute("userId") Long userId ) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, limit));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }


     @PostMapping("/read")
    public ResponseEntity<?> readNotifications(Long notificationId) {
         notificationService.readNotification(notificationId);
         return ResponseEntity.status(200).build();
     }
}