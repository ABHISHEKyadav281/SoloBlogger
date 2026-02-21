package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiRequest.CommentDto;
import com.solo.blogger.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="comment/v1")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping(value = "/addComment")
    public ResponseEntity<?>AddComment(@RequestBody CommentDto comment,@RequestHeader("userId") Long userId){
        return ResponseEntity.ok(ApiResponseDto.success(commentService.addComment(comment,userId)));
    }

    @GetMapping(value = "/getComments")
    public ResponseEntity<?>getCommentsForPost(@RequestParam("postId") long postId){
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @GetMapping(value = "/getComments/replies")
    public ResponseEntity<?>getRepliesForComments(@RequestParam("parentId") Long parentId){
        return ResponseEntity.ok(commentService.getRepliesForComments(parentId));
    }

    @DeleteMapping(value = "/deleteComment")
    public ResponseEntity<?>deleteComment(@RequestBody CommentDto commentDto){
        return ResponseEntity.ok(ApiResponseDto.success("Comment deleted succesfully!"));
    }
}
