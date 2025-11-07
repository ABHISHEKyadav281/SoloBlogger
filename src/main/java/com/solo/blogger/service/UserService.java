package com.solo.blogger.service;


import com.solo.blogger.dto.UserDto;
import com.solo.blogger.dto.appResponse.SuccessResponse;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService  {

    @Autowired
    private UserRepository userRepository;
//
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
    public ResponseEntity<?> register(UserDto userDto){

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        userRepository.save(user);
        return ResponseEntity.ok().body(SuccessResponse.builder().statusCode("202").data("User Registered successfully").build());
    }

}
