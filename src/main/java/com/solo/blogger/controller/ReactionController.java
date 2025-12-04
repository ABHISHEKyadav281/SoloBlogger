package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.enums.ReactionType;
import com.solo.blogger.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/reaction")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/post/react")
    public ResponseEntity<?> reactToPost(@RequestParam Long postId,
                                         @RequestParam ReactionType reactionType,
                                         @RequestAttribute("userId") Long userId) {
        reactionService.reactPost(userId, postId, reactionType);
        return ResponseEntity.ok(ApiResponseDto.success("Reaction updated successfully"));
    }

//    @PostMapping("/comment/{commentId}/{reactionType}")
//    public ResponseEntity<?> reactToComment(@PathVariable Long commentId,
//                                                 @PathVariable ReactionType reactionType,
//                                                 @RequestParam Long userId) {
//        reactionService.reactToComment(userId, commentId, reactionType);
//        return ResponseEntity.ok(ApiResponseDto.success("Reaction updated successfully"));
//    }

}
