package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiRequest.CommentDto;
import com.solo.blogger.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.regions.servicemetadata.ApiSagemakerServiceMetadata;

@RestController
@RequestMapping(value = "/api/comment/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(value = "/addComment")
    public ResponseEntity<?> AddComment(@RequestBody CommentDto comment, @RequestHeader("userId") Long userId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(commentService.addComment(comment, userId)));
    }

    @GetMapping(value = "/getComments")
    public ResponseEntity<?> getCommentsForPost(@RequestParam("postId") long postId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(commentService.getCommentsForPost(postId)));
    }

    @GetMapping(value = "/getComments/replies")
    public ResponseEntity<?> getRepliesForComments(@RequestParam("parentId") Long parentId) {
        return ResponseEntity.ok().body(ApiResponseDto.success(commentService.getRepliesForComments(parentId)));
    }

    @DeleteMapping(value = "/deleteComment")
    public ResponseEntity<?> deleteComment(@RequestBody CommentDto commentDto) { //TODO
        return ResponseEntity.ok().body(ApiResponseDto.success("Comment deleted succesfully!"));
    }
}
