package com.solo.blogger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseResponse {
    private final String errCode;
    private final String errMsg;
}
