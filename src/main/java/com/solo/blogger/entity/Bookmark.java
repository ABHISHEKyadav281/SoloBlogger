package com.solo.blogger.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_bookmarked_by")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long post;

    @Column(name = "user_id")
    private Long user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}


