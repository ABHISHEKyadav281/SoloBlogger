package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiRequest.UploadUrlRequest;
import com.solo.blogger.dto.apiRequest.UserDetailsReqDto;
import com.solo.blogger.dto.responseFactory.SuccessResponse;
import com.solo.blogger.service.S3Service;
import com.solo.blogger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final S3Service s3Service;
    private final UserService userService;

    @GetMapping("/details")
    public ResponseEntity<?> userDetails(@RequestParam("bloggerId") Long bloggerId,
                                         @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(userService.userDetails(bloggerId, userId)));
    }

    @PostMapping("/profile/upload-url")
    public ResponseEntity<?> generateUploadUrl(@RequestBody UploadUrlRequest request) {
        return ResponseEntity.ok().body(ApiResponseDto.success(s3Service.generateProfileUploadUrl(request)));
    }

    @PostMapping("/modify/details")
    public ResponseEntity<?> modifyDetails(@RequestBody UserDetailsReqDto req, @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(userService.modifyUserDetails(req, userId)));
    }

}


