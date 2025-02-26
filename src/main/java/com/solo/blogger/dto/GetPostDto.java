package com.solo.blogger.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPostDto {

        private long id;

        private long user_id;

}
