package com.solo.blogger.dto.responseFactory;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse<T> {
    String statusCode ;
    T data;
}
