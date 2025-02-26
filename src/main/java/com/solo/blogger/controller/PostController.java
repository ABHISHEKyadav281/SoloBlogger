package com.solo.blogger.controller;

import com.solo.blogger.dto.GetPostDto;
import com.solo.blogger.dto.PostDto;
import com.solo.blogger.model.Post;
import com.solo.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/post/v1")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping(value = "/addPost")
    public ResponseEntity<?>AddPost(@RequestBody PostDto postDto){
        System.out.println("add post controller called");
        Post savedPost=postService.addPost(postDto);
        return ResponseEntity.ok("Post created successfully!");
    }

    @GetMapping(value = "/getAllPosts")
    public ResponseEntity<?>AllPosts(){
        List<PostDto> allPosts=postService.getAllPost();
        return ResponseEntity.ok(allPosts);
    }

    @PostMapping(value = "/getPostById")
    public ResponseEntity<?>PostById(@RequestBody GetPostDto getPostDto){
        PostDto userPost=postService.PostById(getPostDto);
        return ResponseEntity.ok(userPost);
    }

    @PostMapping(value = "/getPostByUserId")
    public ResponseEntity<?>PostByUserId(@RequestBody GetPostDto getPostDto){
        List<PostDto> userPost=postService.PostByUserId(getPostDto);
        return ResponseEntity.ok(userPost);
    }

    @DeleteMapping(value= "/deletePostById")
    public ResponseEntity<?> deletePost(@RequestBody GetPostDto getPostDto){
        postService.deletePost(getPostDto);
        return ResponseEntity.ok("post deleted Successfully");
    }

}
