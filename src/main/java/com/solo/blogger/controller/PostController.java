package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.dto.apiRequest.PostDto;
import com.solo.blogger.entity.Post;
import com.solo.blogger.service.FanOutService;
import com.solo.blogger.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/post/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;
    private final FanOutService fanOutService;

    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> CreatePostAlt(
            @ModelAttribute PostDto postDto,
            @RequestAttribute("userId") Long userId
    ) {
        try {
            Post savedPost = postService.createPost(postDto, userId);
            fanOutService.fanOut(savedPost);
            return ResponseEntity.ok(ApiResponseDto.success("Post created successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("500", "Failed to create post: " + e.getMessage()));
        }
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
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestAttribute("userId") Long userId
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
                sortOrder,
                userId
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

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long postId,
                                                       @RequestAttribute("userId") Long userId) {
        PostResponseDto post = postService.getPostById(postId, userId);
        return ResponseEntity.ok(post);
    }


    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getPostsByUser(
            @RequestParam("bloggerId") Long bloggerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestAttribute("userId") Long userId
    ) {
        Page<PostResponseDto> postsPage = postService.getPostsByUserId(bloggerId, page - 1, limit, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postsPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", postsPage.getTotalPages());
        response.put("totalPosts", postsPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String status,
            @RequestAttribute("userId") Long userId
    ) {
        Page<PostResponseDto> feedPage = postService.getFeedForUser(
                userId, page - 1, limit, category, search, featured, status
        );

        Map<String, Object> response = new HashMap<>();
        response.put("posts", feedPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", feedPage.getTotalPages());
        response.put("totalPosts", feedPage.getTotalElements());
        response.put("hasMore", feedPage.hasNext());
        response.put("hasPrevious", feedPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

}