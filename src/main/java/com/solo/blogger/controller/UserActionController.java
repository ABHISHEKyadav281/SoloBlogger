package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.entity.User;
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

    @PostMapping("/unbookmark")
    public ResponseEntity<?> unBookmarkPost(@RequestParam Long postId,
                                          @RequestAttribute("userId") Long userId) {
        userActionService.unBookmarkPost(postId,userId);
        return ResponseEntity.ok(ApiResponseDto.success("Post unBookmarked successfully"));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestParam Long bloggerId,
                                            @RequestAttribute("userId") Long userId) {
        userActionService.subscribeBlogger(bloggerId,userId);
        return ResponseEntity.ok(ApiResponseDto.success("Subscribed user successfully"));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unSubscribe(@RequestParam Long bloggerId,
                                       @RequestAttribute("userId") Long userId) {
        userActionService.unSubscribeBlogger(bloggerId,userId);
        return ResponseEntity.ok(ApiResponseDto.success("unSubscribed user successfully"));
    }

    @GetMapping("/my/subscribers")
    public ResponseEntity<?> getSubscribers(@RequestAttribute("userId") Long userId) {
       List<String> mySubscribersList= userActionService.subscribersList(userId);
        return ResponseEntity.ok(ApiResponseDto.success(mySubscribersList));
    }

}
