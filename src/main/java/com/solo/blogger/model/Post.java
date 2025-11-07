package com.solo.blogger.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String excerpt;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(nullable = false, length = 50)
    private String category;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "allow_comments", nullable = false)
    private Boolean allowComments = true;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(name = "comments_count")
    private Long commentsCount = 0L;

    @Column(name = "likes_count")
    private Long likesCount = 0L;

    @Column(name = "views_count")
    private Long viewsCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public enum PostStatus {
        DRAFT, PUBLISHED, SCHEDULED
    }

    public enum PostVisibility {
        PUBLIC, PRIVATE, FOLLOWERS
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (commentsCount == null) commentsCount = 0L;
        if (likesCount == null) likesCount = 0L;
        if (viewsCount == null) viewsCount = 0L;
        if (allowComments == null) allowComments = true;
        if (featured == null) featured = false;
        if (status == null) status = PostStatus.DRAFT;
        if (visibility == null) visibility = PostVisibility.PUBLIC;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}