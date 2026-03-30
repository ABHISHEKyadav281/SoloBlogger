package com.solo.blogger.controller;

import com.solo.blogger.dto.apiRequest.UploadUrlRequest;
import com.solo.blogger.dto.apiRequest.UserDetailsReqDto;
import com.solo.blogger.service.S3Service;
import com.solo.blogger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserService userService;

    @GetMapping("/details")
    public ResponseEntity<?> userDetails(@RequestParam("bloggerId") Long bloggerId,
                                         @RequestAttribute("userId") Long userId) {
        return userService.userDetails(bloggerId, userId);
    }

    @PostMapping("/profile/upload-url")
    public ResponseEntity<?> generateUploadUrl(@RequestBody UploadUrlRequest request) {
        System.out.println("controller");
        return s3Service.generateProfileUploadUrl(request);
    }

    @PostMapping("/modify/details")
    public ResponseEntity<?> modifyDetails(@RequestBody UserDetailsReqDto req, @RequestAttribute("userId") Long userId) {
        return userService.modifyUserDetails(req, userId);
    }

}


