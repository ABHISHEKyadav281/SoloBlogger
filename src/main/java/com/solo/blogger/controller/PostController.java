package com.solo.blogger.controller;

import com.solo.blogger.dto.ApiResponseDto;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.dto.apiRequest.PostDto;
import com.solo.blogger.entity.Post;
import com.solo.blogger.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> CreatePostAlt(
            @ModelAttribute PostDto postDto,
            @RequestAttribute("userId") Long userId
    ) {
        System.out.println("Create post alt controller called");

        try {
            Post savedPost = postService.createPost(postDto, userId);
            return ResponseEntity.ok(ApiResponseDto.success("Post created successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("500","Failed to create post: " + e.getMessage()));
        }
    }


    // fetch details from post table and regarding every post need to fetch image from immemory files based on image url
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

//    details from post table &image from file system
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        PostResponseDto post = postService.getPostById(id);
        return ResponseEntity.ok(post);
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