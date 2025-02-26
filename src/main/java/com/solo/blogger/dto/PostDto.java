package com.solo.blogger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PostDto {

    private long id;

    @NotBlank(message = "userId ir required")
    private long user_id;

    @NotBlank(message = "Title is required")
    private String Title;

    @NotBlank(message = "Provide some content")
    private String Content;

    private String picture;

    private String Category;

    private List<String> Tags;

    private Date time;

}
