package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.AuthRequest;
import com.solo.blogger.dto.apiRequest.UserDto;
import com.solo.blogger.repository.UserRepository;
import com.solo.blogger.service.AuthService;
import com.solo.blogger.service.UserService;
import com.solo.blogger.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth/v1")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "signup")
    public ResponseEntity<?> Register(@RequestBody UserDto userDto) {
        return ResponseEntity.ok().body(ApiResponseDto.success(userService.register(userDto)));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok().body(ApiResponseDto.success(authService.login(authRequest)));

    }

}
