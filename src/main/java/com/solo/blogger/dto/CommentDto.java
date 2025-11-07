package com.solo.blogger.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private long id;

    private String content;

    private long post_id;

    private long parent_comment_id;
}
