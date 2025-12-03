package com.solo.blogger.dto.responseFactory;

public class ErrorResponse  {
        String errCode ;
        String errMsg ;
        ErrorResponse(String errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
}
