package com.solo.blogger.service;

import com.solo.blogger.dto.PostDto;
import com.solo.blogger.dto.PostResponseDto;
import com.solo.blogger.model.Post;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailService emailService;

    public Post createPost(PostDto postDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Post post = Post.builder()
                .user(user)  // Use User object, not userId
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .excerpt(postDto.getExcerpt())
                .category(postDto.getCategory())
                .tags(postDto.getTags() != null ? postDto.getTags() : new ArrayList<>())
                .coverImage(postDto.getCoverImage())
                .status(postDto.getStatus() != null ? postDto.getStatus() : Post.PostStatus.DRAFT)
                .visibility(postDto.getVisibility() != null ? postDto.getVisibility() : Post.PostVisibility.PUBLIC)
                .allowComments(postDto.getAllowComments() != null ? postDto.getAllowComments() : true)
                .featured(postDto.getFeatured() != null ? postDto.getFeatured() : false)
                .publishDate(postDto.getStatus() == Post.PostStatus.PUBLISHED ? LocalDateTime.now() : postDto.getPublishDate())
                .commentsCount(0L)
                .likesCount(0L)
                .viewsCount(0L)
                .build();

        Post savedPost= postRepository.save(post);
        String message= String.format("user %s created a new post: %s", user.getUsername(), post.getTitle());
        kafkaTemplate.send("post-created", message);
//        emailService.sendNewPostNotification("yyadavabhishek9@gmail.com" ,user.getUsername(),post.getTitle());

        return savedPost;
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPosts(
            int page,
            int limit,
            String category,
            String status,
            String visibility,
            String search,
            Boolean featured,
            String sortBy,
            String sortOrder
    ) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

        Page<Post> postsPage;

        // Apply filters
        if (search != null && !search.isEmpty()) {
            postsPage = postRepository.searchPosts(search, pageable);
        } else if (category != null && featured != null) {
            postsPage = postRepository.findByCategoryAndFeatured(category, featured, pageable);
        } else if (category != null) {
            postsPage = postRepository.findByCategory(category, pageable);
        } else if (featured != null && featured) {
            postsPage = postRepository.findByFeaturedTrue(pageable);
        } else if (status != null) {
            postsPage = postRepository.findByStatus(Post.PostStatus.valueOf(status), pageable);
        } else {
            // Default: get all published posts
            postsPage = postRepository.findByStatus(Post.PostStatus.PUBLISHED, pageable);
        }

        return postsPage.map(this::convertToResponseDto);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Increment view count
        post.setViewsCount(post.getViewsCount() + 1);
        postRepository.save(post);

        return convertToResponseDto(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getFeaturedPosts(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage = postRepository.findByFeaturedTrueAndStatus(
                Post.PostStatus.PUBLISHED,
                pageable
        );
        return postsPage.map(this::convertToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostsByUserId(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage = postRepository.findByUserId(userId, pageable);
        return postsPage.map(this::convertToResponseDto);
    }

//    public Post createPost(PostDto postDto, Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
//
//        Post post = Post.builder()
//                .user(user)
//                .title(postDto.getTitle())
//                .content(postDto.getContent())
//                .excerpt(postDto.getExcerpt())
//                .category(postDto.getCategory())
//                .tags(postDto.getTags() != null ? postDto.getTags() : new ArrayList<>())
//                .coverImage(postDto.getCoverImage())
//                .status(postDto.getStatus() != null ? postDto.getStatus() : Post.PostStatus.DRAFT)
//                .visibility(postDto.getVisibility() != null ? postDto.getVisibility() : Post.PostVisibility.PUBLIC)
//                .allowComments(postDto.getAllowComments() != null ? postDto.getAllowComments() : true)
//                .featured(postDto.getFeatured() != null ? postDto.getFeatured() : false)
//                .publishDate(postDto.getStatus() == Post.PostStatus.PUBLISHED ?
//                        LocalDateTime.now() : postDto.getPublishDate())
//                .commentsCount(0L)
//                .likesCount(0L)
//                .viewsCount(0L)
//                .build();
//
//        return postRepository.save(post);
//    }

    // Convert Post entity to PostResponseDto
    private PostResponseDto convertToResponseDto(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .coverImage(post.getCoverImage())
                .category(post.getCategory())
                .tags(post.getTags())
                .status(post.getStatus())
                .visibility(post.getVisibility())
                .publishDate(post.getPublishDate())
                .allowComments(post.getAllowComments())
                .featured(post.getFeatured())
                .commentsCount(post.getCommentsCount())
                .likesCount(post.getLikesCount())
                .viewsCount(post.getViewsCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .user(PostResponseDto.UserSummaryDto.builder()
                        .id(post.getUser().getId())
                        .username(post.getUser().getUsername())
                        .email(post.getUser().getEmail())
                        .build())
                .build();
    }
}
































//package com.solo.blogger.service;
//
//import com.solo.blogger.dto.GetPostDto;
//import com.solo.blogger.dto.PostDto;
//import com.solo.blogger.model.Post;
//import com.solo.blogger.model.User;
//import com.solo.blogger.repository.PostRepository;
//import com.solo.blogger.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
//@Service
//public class PostService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    public Post createPost(PostDto postDto, Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
//
//        Post post = Post.builder()
//                .user(user)  // Use User object, not userId
//                .title(postDto.getTitle())
//                .content(postDto.getContent())
//                .excerpt(postDto.getExcerpt())
//                .category(postDto.getCategory())
//                .tags(postDto.getTags() != null ? postDto.getTags() : new ArrayList<>())
//                .coverImage(postDto.getCoverImage())
//                .status(postDto.getStatus() != null ? postDto.getStatus() : Post.PostStatus.DRAFT)
//                .visibility(postDto.getVisibility() != null ? postDto.getVisibility() : Post.PostVisibility.PUBLIC)
//                .allowComments(postDto.getAllowComments() != null ? postDto.getAllowComments() : true)
//                .featured(postDto.getFeatured() != null ? postDto.getFeatured() : false)
//                .publishDate(postDto.getStatus() == Post.PostStatus.PUBLISHED ? LocalDateTime.now() : postDto.getPublishDate())
//                .commentsCount(0L)
//                .likesCount(0L)
//                .viewsCount(0L)
//                .build();
//
//        return postRepository.save(post);
//    }
//
////    @Async("postExecutor")
////    public CompletableFuture<Post> addPostV2(PostDto postDto){
////        System.out.println("Thread Name: " + Thread.currentThread().getName());
////        System.out.println("add post service called");
////        User user = userRepository.findById(postDto.getUser_id())
////                .orElseThrow(() -> new RuntimeException("User not found with ID: " + postDto.getUser_id()));
////        System.out.println("user: "+user);
////        Post post=Post.builder()
////                .user(user)
////                .Title(postDto.getTitle())
////                .Content(postDto.getContent())
////                .Category(postDto.getCategory())
////                .Tags(postDto.getTags())
////                .picture(postDto.getPicture())
////                .createdAt(new Date())
////                .build();
////
////        Post savedPost = postRepository.save(post);
////        return CompletableFuture.completedFuture(savedPost);
////    }
//
//    public List<PostDto> getAllPost(){
//        List<Post> posts = postRepository.findAll();
//        return posts.stream().map(post -> PostDto.builder()
//                .id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .excerpt(post.getExcerpt())
//                .category(post.getCategory())
//                .tags(post.getTags())
//                .coverImage(post.getCoverImage())
//                .status(post.getStatus())
//                .visibility(post.getVisibility())
//                .allowComments(post.getAllowComments())
//                .featured(post.getFeatured())
//                .publishDate(post.getPublishDate())
//                .commentsCount(post.getCommentsCount())
//                .likesCount(post.getLikesCount())
//                .viewsCount(post.getViewsCount())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .build()
//
//        ).collect(Collectors.toList());
//    }
//
//    public PostDto PostById(GetPostDto getPostDto){
//        Post post = postRepository.findById(getPostDto.getId()).orElseThrow(()->new RuntimeException("Post not found"));
//        System.out.println(getPostDto.getId());
//        System.out.println(post);
//        return  PostDto.builder().id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .excerpt(post.getExcerpt())
//                .category(post.getCategory())
//                .tags(post.getTags())
//                .coverImage(post.getCoverImage())
//                .status(post.getStatus())
//                .visibility(post.getVisibility())
//                .allowComments(post.getAllowComments())
//                .featured(post.getFeatured())
//                .publishDate(post.getPublishDate())
//                .commentsCount(post.getCommentsCount())
//                .likesCount(post.getLikesCount())
//                .viewsCount(post.getViewsCount())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .build();
//    }
//
//    public List<PostDto> PostByUserId(GetPostDto getPostDto){
//        List<Post> posts = postRepository.findByUserId(getPostDto.getUser_id());
//        System.out.println(getPostDto.getId());
//        System.out.println(posts);
//        return posts.stream().map(post -> PostDto.builder().id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .excerpt(post.getExcerpt())
//                .category(post.getCategory())
//                .tags(post.getTags())
//                .coverImage(post.getCoverImage())
//                .status(post.getStatus())
//                .visibility(post.getVisibility())
//                .allowComments(post.getAllowComments())
//                .featured(post.getFeatured())
//                .publishDate(post.getPublishDate())
//                .commentsCount(post.getCommentsCount())
//                .likesCount(post.getLikesCount())
//                .viewsCount(post.getViewsCount())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .build()
//        ).collect(Collectors.toList());
//    }
//
//    public void deletePost(GetPostDto getPostDto){
//
//        User user=userRepository.findById(getPostDto.getUser_id()).orElseThrow(()->new RuntimeException("user not found"));
//
//        if(user.getId()!=getPostDto.getUser_id()) throw new RuntimeException("you are not authorize to delete this post");
//        Post post=postRepository.findById(getPostDto.getId()).orElseThrow(()->new RuntimeException("Post does not exist"));
//        postRepository.delete(post);
//    }
//
//
//}
