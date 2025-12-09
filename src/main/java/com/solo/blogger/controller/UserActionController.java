package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.service.UserActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user/action/")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

//    @PostMapping("/subscribe/{subscriberId}/{bloggerId}")
//    public ResponseEntity<?> subscribe(@PathVariable Long subscriberId, @PathVariable Long bloggerId){
//        System.out.println("hii");
//        subscriptionService.subscribe(subscriberId,bloggerId);
//        return ResponseEntity.ok(ApiResponseDto.success("Subscribed succesfully"));
//    }

//    @DeleteMapping("unSubscribe/{subscriberId}/{bloggerId}")
//    public ResponseEntity<?> unSubscribe(@PathVariable Long subscriberId, @PathVariable Long bloggerId){
//        subscriptionService.unSubscribe(subscriberId,bloggerId);
//        return ResponseEntity.ok(ApiResponseDto.success("unSubscribed successfully"));
//    }

    @PostMapping("/bookmark")
    public ResponseEntity<?> bookmarkPost(@RequestParam Long postId,
                                          @RequestAttribute("userId") Long userId) {
        userActionService.bookmarkPost(postId,userId);
        return ResponseEntity.ok(ApiResponseDto.success("Post bookmarked successfully"));
    }

    @GetMapping("/bookmarked/posts")
    public ResponseEntity<?> getBookmarkedPosts(@RequestAttribute("userId") Long userId) {
        List<PostResponseDto> response=userActionService.getBookmarkedPosts(userId);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

}
