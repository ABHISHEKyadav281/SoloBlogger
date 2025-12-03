package com.solo.blogger.dto.apiResponse;

import com.solo.blogger.entity.Post;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long id;

    // User ID - not needed in request if you're getting it from authentication
    // Include only if explicitly sending it
//    private Long userId;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 1000, message = "Excerpt cannot exceed 1000 characters")
    private String excerpt;

    @Size(max = 500, message = "Cover image URL cannot exceed 500 characters")
    private String coverImage;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private List<String> tags;

    @NotNull(message = "Status is required")
    private Post.PostStatus status;

    @NotNull(message = "Visibility is required")
    private Post.PostVisibility visibility;

    private LocalDateTime publishDate;

    private Boolean allowComments;

    private Boolean featured;

    // For response DTOs
    private Long commentsCount;
    private Long likesCount;
    private Long viewsCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested user info for response
    private UserSummaryDto user;

    @Data
    @Builder
    public static class UserSummaryDto {
        private Long id;
        private String username;
        private String profilePicture;
    }
}