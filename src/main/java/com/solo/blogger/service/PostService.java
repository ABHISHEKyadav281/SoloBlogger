package com.solo.blogger.service;

import com.solo.blogger.dto.apiResponse.PostCreatedEvent;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.dto.apiRequest.PostDto;
import com.solo.blogger.entity.Post;
import com.solo.blogger.entity.PostLike;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.*;
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

    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final EmailService emailService;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private S3FileStorageService s3FileStorageService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    public Post createPost(PostDto postDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ✅ Generate image URL for uploaded file
        String imageUrl = "";
        if (postDto.getCoverImage() != null && !postDto.getCoverImage().isEmpty()) {
            String s3Key = s3FileStorageService.storeFile(postDto.getCoverImage(), postDto.getTitle());
            imageUrl = s3FileStorageService.getFileUrl(s3Key);
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
        if (isKafkaEnabled) {
            PostCreatedEvent event = new PostCreatedEvent(
                    post.getId(),
                    user.getId(),
                    user.getUsername(),
                    post.getTitle()
            );
            kafkaTemplate.send("post-created", event);
        }
        else{
            notificationService.notificationEntry(user.getId(), post.getId(), post.getTitle(),user.getUsername());
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
            String sortOrder,
            Long userId
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
            postsPage = postRepository.findByStatus(Post.PostStatus.PUBLISHED, pageable);
        }

        return postsPage.map(post->convertToResponseDto2(post, userId));
    }



    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id, Long userId) {
        try {
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

            long count = post.getViewsCount();
            post.setViewsCount(count + 1);
            postRepository.save(post);

            boolean isLiked = false;
            PostLike like = postLikeRepository.findByPostIdAndUserId(post.getId(), userId).orElse(null);
            if (like != null) isLiked = true;

            return convertToResponseDto(post, isLiked,userId);

        } catch (RuntimeException e) {
            System.err.println("Error fetching post: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching post with id: " + id);
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve post", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getFeaturedPosts(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage = postRepository.findByFeaturedTrueAndStatus(
                Post.PostStatus.PUBLISHED,
                pageable
        );
        return postsPage.map(post->convertToResponseDto2(post,null));
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostsByUserId(Long bloggerId, int page, int limit, Long userId) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage = postRepository.findByUserId(bloggerId, pageable);
        return postsPage.map(post->convertToResponseDto2(post,userId));
    }

    public PostResponseDto convertToResponseDto(Post post,boolean isLiked,Long currentUserId) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        Long followersCount=subscriptionRepository.countByBloggerId(post.getUserId());
        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = subscriptionRepository.existsByBloggerIdAndSubscriberId(post.getUserId(), currentUserId);
        }
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
                .subscribed(isFollowing)
                .isIsLiked(isLiked)
                .author(user==null ? null : PostResponseDto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .followers(followersCount)
                        .build());

        return builder.build();
    }

    public PostResponseDto convertToResponseDto2(Post post,Long currentUserId) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        Long followersCount=subscriptionRepository.countByBloggerId(post.getUserId());
        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = subscriptionRepository.existsByBloggerIdAndSubscriberId(post.getUserId(), currentUserId);
        }
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
                .subscribed(isFollowing)
                .author(user==null ? null : PostResponseDto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .followers(followersCount)
                        .build());

        if (currentUserId != null) {
            boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            builder.isBookmarked(isBookmarked);
        } else {
            builder.isBookmarked(false);
        }
        return builder.build();
    }
}
