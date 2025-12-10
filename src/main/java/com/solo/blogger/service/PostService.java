package com.solo.blogger.service;

import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.dto.apiRequest.PostDto;
import com.solo.blogger.entity.Post;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    static boolean isKafkaEnabled= false;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailService emailService;

    @Transactional
    public Post createPost(PostDto postDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ✅ Generate image URL for uploaded file
        String imageUrl = "";
        if (postDto.getCoverImage() != null && !postDto.getCoverImage().isEmpty()) {
            String savedImage = fileStorageService.storeFile(postDto.getCoverImage());
            imageUrl = fileStorageService.getFileUrl(savedImage);
        }

        Post post = Post.builder()
                .userId(userId)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .excerpt(postDto.getExcerpt())
                .category(postDto.getCategory())
                .tags(postDto.getTags() != null ? postDto.getTags() : new ArrayList<>())
                .coverImage(imageUrl)
                .status(postDto.getStatus() != null ? postDto.getStatus() : Post.PostStatus.DRAFT)
                .visibility(postDto.getVisibility() != null ? postDto.getVisibility() : Post.PostVisibility.PUBLIC)
                .allowComments(postDto.getAllowComments() != null ? postDto.getAllowComments() : true)
                .featured(postDto.getFeatured() != null ? postDto.getFeatured() : false)
                .publishDate(postDto.getStatus() == Post.PostStatus.PUBLISHED ? LocalDateTime.now() : postDto.getPublishDate())
                .commentsCount(0L)
                .likesCount(0L)
                .viewsCount(0L)
                .build();

        Post savedPost = postRepository.save(post);
        String message = String.format("user %s created a new post: %s", user.getUsername(), post.getTitle());
        if (isKafkaEnabled) {
            kafkaTemplate.send("post-created", message);
        }
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
//    private PostResponseDto convertToResponseDto(Post post) {
//        return PostResponseDto.builder()
//                .id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .excerpt(post.getExcerpt())
//                .coverImage(post.getCoverImage())
//                .category(post.getCategory())
//                .tags(post.getTags())
//                .status(post.getStatus())
//                .visibility(post.getVisibility())
//                .publishDate(post.getPublishDate())
//                .allowComments(post.getAllowComments())
//                .featured(post.getFeatured())
//                .commentsCount(post.getCommentsCount())
//                .likesCount(post.getLikesCount())
//                .viewsCount(post.getViewsCount())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .user(PostResponseDto.UserSummaryDto.builder()
//                        .id(post.getUser().getId())
//                        .username(post.getUser().getUsername())
//                        .email(post.getUser().getEmail())
//                        .build())
//                .build();
//    }

    public PostResponseDto convertToResponseDto(Post post) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        PostResponseDto.PostResponseDtoBuilder builder = PostResponseDto.builder()
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
                .author(user==null ? null : PostResponseDto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build());

        // Load and encode the cover image if it exists
        if (post.getCoverImage() != null && !post.getCoverImage().isEmpty()) {
            String fileName = post.getCoverImage();

            // Extract filename from URL if it's a local file
            if (fileName.startsWith("/api/files/")) {
                fileName = fileName.substring("/api/files/".length());
            }

            // Load and encode the image only if it's a local file (not external URL)
            if (!fileName.startsWith("http")) {
                try {
                    Resource resource = fileStorageService.loadFileAsResource(fileName);
                    byte[] imageBytes = Files.readAllBytes(resource.getFile().toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    builder.coverImageData("data:image/jpeg;base64," + base64Image);
                } catch (Exception e) {
                    System.err.println("Could not load image: " + fileName);
                    builder.coverImageData(null);
                }
            }
        }

        return builder.build();
    }
}
