package com.solo.blogger.dto;

public class ErrorResponse extends BaseResponse{
    public ErrorResponse(String errCode, String errMsg) {
        super(errCode, errMsg);
    }
}
