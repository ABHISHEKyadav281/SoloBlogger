package com.solo.blogger.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long postId;
    private String authorName;
    private String message;
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
