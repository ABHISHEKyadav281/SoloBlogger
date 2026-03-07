package com.solo.blogger.controller;

import com.solo.blogger.dto.apiResponse.NotificationResponse;
import com.solo.blogger.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
//    private final SseService sseService;

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


    // GET /api/notifications/subscribe  (SSE - real time)
//    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter subscribe(
//            @AuthenticationPrincipal Long userId) {          // ← from token
//
//        return sseService.subscribe(userId);
//    }
}