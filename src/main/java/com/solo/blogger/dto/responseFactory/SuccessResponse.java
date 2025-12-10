package com.solo.blogger.dto.responseFactory;

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
