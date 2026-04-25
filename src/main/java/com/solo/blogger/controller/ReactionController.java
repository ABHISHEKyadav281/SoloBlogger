package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.enums.ReactionType;
import com.solo.blogger.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/reaction")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/post/react")
    public ResponseEntity<?> reactToPost(@RequestParam Long postId,
                                         @RequestParam ReactionType reactionType,
                                         @RequestAttribute("userId") Long userId) {
        reactionService.reactPost(userId, postId, reactionType);
        return ResponseEntity.ok().body(ApiResponseDto.success("Reaction updated successfully"));
    }

    @GetMapping("/post/like/count")
    public ResponseEntity<?> reactToPost(@RequestParam Long postId) {
        long count=reactionService.postReactionCount( postId);
        return ResponseEntity.ok().body(ApiResponseDto.success(count));
    }

    @GetMapping("post/isLiked")
    public ResponseEntity<?> isLiked(@RequestParam Long postId,
                                         @RequestAttribute("userId") Long userId) {
        boolean isLiked=reactionService.postIsLiked(userId, postId);
        return ResponseEntity.ok().body(ApiResponseDto.success(isLiked));
    }

}
