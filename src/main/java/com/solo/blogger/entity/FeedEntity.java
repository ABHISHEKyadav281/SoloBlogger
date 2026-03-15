package com.solo.blogger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feed_entries", indexes = {
        @Index(name = "idx_feed_user_created", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_feed_post", columnList = "post_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}