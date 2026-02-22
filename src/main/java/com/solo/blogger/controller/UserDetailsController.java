package com.solo.blogger.controller;

import com.solo.blogger.dto.responseFactory.SuccessResponse;
import com.solo.blogger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/details")
public class UserDetailsController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> userDetails(@RequestParam("bloggerId") Long bloggerId,
                                         @RequestAttribute("userId") Long userId) {
        return userService.userDetails(bloggerId,userId);
    }
}


