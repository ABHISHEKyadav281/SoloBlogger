package com.solo.blogger.dto.apiResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long likesCount;
    private int repliesCount;
    private LocalDateTime createdAt;
    private Boolean isLiked;
    private UserDetailsDto author;
}