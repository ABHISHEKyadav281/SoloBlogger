package com.solo.blogger.controller;


import com.solo.blogger.dto.AuthRequest;
import com.solo.blogger.dto.UserDto;
import com.solo.blogger.model.User;
import com.solo.blogger.service.AuthService;
import com.solo.blogger.service.UserService;
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

    @PostMapping(value = "signup")
    public ResponseEntity<?> Register(@RequestBody UserDto userDto){
        User savedUser = userService.register(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping(value="signin")
    public ResponseEntity<String>Login(@RequestBody AuthRequest authRequest){
        String token=authService.login(authRequest);
        return ResponseEntity.ok(token);
    }

}
