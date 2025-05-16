package com.solo.blogger.controller;


import com.solo.blogger.dto.AuthRequest;
import com.solo.blogger.dto.UserDto;
import com.solo.blogger.model.User;
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
@RequestMapping(value = "/auth/v1")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "signup")
    public ResponseEntity<?> Register(@RequestBody UserDto userDto){
        System.out.println("hii abhi");
        User savedUser = userService.register(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/signin")
    public String login(@RequestBody AuthRequest authRequest) {
        System.out.println("hi login api");
        // Validate user credentials (implement your user service)
        String token = jwtUtil.generateToken(authRequest.getUsername());
        return token;
    }

}
