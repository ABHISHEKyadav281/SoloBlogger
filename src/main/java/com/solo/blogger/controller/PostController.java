package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.PostDto;
import com.solo.blogger.dto.PostResponseDto;
import com.solo.blogger.model.Post;
import com.solo.blogger.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/post/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;

    @PostMapping(value = "createPost")
    public ResponseEntity<?>CreatePost(@RequestBody PostDto post,  @RequestAttribute("userId") Long userId){
        System.out.println("add post controller called");
        Post savedPost=postService.createPost(post,userId);
        return ResponseEntity.ok(ApiResponseDto.success("Post created successfully!"));
    }

    @GetMapping("/allposts")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Page<PostResponseDto> postsPage = postService.getAllPosts(
                page - 1,
                limit,
                category,
                status,
                visibility,
                search,
                featured,
                sortBy,
                sortOrder
        );

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postsPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", postsPage.getTotalPages());
        response.put("totalPosts", postsPage.getTotalElements());
        response.put("hasMore", postsPage.hasNext());
        response.put("hasPrevious", postsPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        PostResponseDto post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit
    ) {
        Page<PostResponseDto> postsPage = postService.getFeaturedPosts(page - 1, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postsPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", postsPage.getTotalPages());
        response.put("totalPosts", postsPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<PostResponseDto> postsPage = postService.getPostsByUserId(userId, page - 1, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postsPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", postsPage.getTotalPages());
        response.put("totalPosts", postsPage.getTotalElements());

        return ResponseEntity.ok(response);
    }
}












//package com.solo.blogger.controller;
//
//import com.solo.blogger.dto.ApiResponseDto;
//import com.solo.blogger.dto.GetPostDto;
//import com.solo.blogger.dto.PostDto;
//import com.solo.blogger.model.Post;
//import com.solo.blogger.service.PostService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@RestController
//@RequestMapping(value = "/post")
//public class PostController {
//
//    @Autowired
//    private PostService postService;
//
//    @PostMapping(value = "/v1/createPost")
//    public ResponseEntity<?>CreatePost(@RequestBody PostDto postDto,  @RequestAttribute("userId") Long userId){
//        System.out.println("add post controller called");
//        Post savedPost=postService.createPost(postDto,userId);
//        return ResponseEntity.ok(ApiResponseDto.success("Post created successfully!"));
//    }
//
////    @PostMapping(value = "/v2/addPost")
////    public CompletableFuture<ResponseEntity<Post>>AddPostV2(@RequestBody PostDto postDto){
////        System.out.println("add post controller called");
////        return postService.addPostV2(postDto).thenApply(ResponseEntity::ok);
////    }
//
//    @GetMapping(value = "/v1/getAllPosts")
//    public ResponseEntity<?>AllPosts(){
//        List<PostDto> allPosts=postService.getAllPost();
//        return ResponseEntity.ok(ApiResponseDto.success(allPosts));
//    }
//
//    @PostMapping(value = "/v1/getPostById")
//    public ResponseEntity<?>PostById(@RequestBody GetPostDto getPostDto){
//        PostDto userPost=postService.PostById(getPostDto);
//        return ResponseEntity.ok(ApiResponseDto.success(userPost));
//    }
//
//    @PostMapping(value = "/v1/getPostByUserId")
//    public ResponseEntity<?>PostByUserId(@RequestBody GetPostDto getPostDto){
//        List<PostDto> userPost=postService.PostByUserId(getPostDto);
//        return ResponseEntity.ok(ApiResponseDto.success(userPost));
//    }
//
//    @DeleteMapping(value= "/v1/deletePostById")
//    public ResponseEntity<?> deletePost(@RequestBody GetPostDto getPostDto){
//        postService.deletePost(getPostDto);
//        return ResponseEntity.ok(ApiResponseDto.success("post deleted Successfully"));
//    }
//
//}
