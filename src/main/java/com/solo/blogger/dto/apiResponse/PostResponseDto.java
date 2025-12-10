package com.solo.blogger.dto.apiResponse;

import com.solo.blogger.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String coverImage;
    private String coverImageData;
    private String category;
    private List<String> tags;
    private Post.PostStatus status;
    private Post.PostVisibility visibility;
    private LocalDateTime publishDate;
    private Boolean allowComments;
    private Boolean featured;
    private Long commentsCount;
    private Long likesCount;
    private Long viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private UserSummaryDto author;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String username;
        private String email;
    }
}