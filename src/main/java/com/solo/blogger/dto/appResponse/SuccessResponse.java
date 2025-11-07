package com.solo.blogger.dto.appResponse;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    String statusCode ;
    String data;
}
