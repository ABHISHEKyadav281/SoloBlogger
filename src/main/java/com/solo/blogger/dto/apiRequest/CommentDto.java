package com.solo.blogger.dto.apiRequest;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private long id;
    private String content;
    private long postId;
    private Long parentId;

}
