package com.solo.blogger.controller;

import com.solo.blogger.dto.GetPostDto;
import com.solo.blogger.dto.PostDto;
import com.solo.blogger.model.Post;
import com.solo.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/post")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping(value = "/v1/addPost")
    public ResponseEntity<?>AddPost(@RequestBody PostDto postDto){
        System.out.println("add post controller called");
        Post savedPost=postService.addPost(postDto);
        return ResponseEntity.ok("Post created successfully!");
    }

    @PostMapping(value = "/v2/addPost")
    public CompletableFuture<ResponseEntity<Post>>AddPostV2(@RequestBody PostDto postDto){
        System.out.println("add post controller called");
        return postService.addPostV2(postDto)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping(value = "/v1/getAllPosts")
    public ResponseEntity<?>AllPosts(){
        List<PostDto> allPosts=postService.getAllPost();
        return ResponseEntity.ok(allPosts);
    }

    @PostMapping(value = "/v1/getPostById")
    public ResponseEntity<?>PostById(@RequestBody GetPostDto getPostDto){
        PostDto userPost=postService.PostById(getPostDto);
        return ResponseEntity.ok(userPost);
    }

    @PostMapping(value = "/v1/getPostByUserId")
    public ResponseEntity<?>PostByUserId(@RequestBody GetPostDto getPostDto){
        List<PostDto> userPost=postService.PostByUserId(getPostDto);
        return ResponseEntity.ok(userPost);
    }

    @DeleteMapping(value= "/v1/deletePostById")
    public ResponseEntity<?> deletePost(@RequestBody GetPostDto getPostDto){
        postService.deletePost(getPostDto);
        return ResponseEntity.ok("post deleted Successfully");
    }

}
