package com.solo.blogger.dto.apiResponse;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
