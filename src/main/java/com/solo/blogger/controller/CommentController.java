package com.solo.blogger.controller;

import com.solo.blogger.dto.CommentDto;
import com.solo.blogger.model.Comment;
import com.solo.blogger.repository.CommentRepository;
import com.solo.blogger.service.CommentService;
import com.solo.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="comment/v1")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping(value = "/addComment")
    public ResponseEntity<?>AddComment(@RequestBody CommentDto commentDto){
        Comment savedComment= commentService.addComment(commentDto);
        return ResponseEntity.ok("Comment added succesfully!");
    }

    @PostMapping(value = "/replyComment")
    public ResponseEntity<?>ReplyComment(@RequestBody CommentDto commentDto){
        Comment savedComment= commentService.replyComment(commentDto);
        return ResponseEntity.ok("Comment reply added succesfully!");
    }

    @DeleteMapping(value = "/deleteComment")
    public ResponseEntity<?>deleteComment(@RequestBody CommentDto commentDto){
        commentService.deleteComment(commentDto);
        return ResponseEntity.ok("Comment deleted succesfully!");
    }
}
