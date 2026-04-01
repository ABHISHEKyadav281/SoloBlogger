package com.solo.blogger.service;

import com.solo.blogger.dto.apiResponse.PostCreatedEvent;
import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.dto.apiRequest.PostDto;
import com.solo.blogger.entity.FeedEntity;
import com.solo.blogger.entity.Post;
import com.solo.blogger.entity.PostLike;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    static boolean isKafkaEnabled = false;

    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final EmailService emailService;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CacheService cacheService;

    @Transactional
    public Post createPost(PostDto postDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String imageUrl = "";
        if (postDto.getCoverImage() != null && !postDto.getCoverImage().isEmpty()) {
            String s3Key = s3Service.storeFile(postDto.getCoverImage(), postDto.getTitle());
            imageUrl = s3Service.getFileUrl(s3Key);
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
        } else {
            notificationService.notificationEntry(user.getId(), post.getId(), post.getTitle(), user.getUsername());
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

        return postsPage.map(post -> convertToResponseDto2(post, userId));
    }


    @Transactional
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
            Long commentsCount = commentRepository.countByPostId(post.getId());
            return convertToResponseDto(post, isLiked, userId, commentsCount);

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
        return postsPage.map(post -> convertToResponseDto2(post, null));
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostsByUserId(Long bloggerId, int page, int limit, Long userId) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postsPage = postRepository.findByUserId(bloggerId, pageable);
        return postsPage.map(post -> convertToResponseDto2(post, userId));
    }

    public PostResponseDto convertToResponseDto(Post post, boolean isLiked, Long currentUserId, Long commentsCount) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        Long followersCount = subscriptionRepository.countByBloggerId(post.getUserId());
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
                .commentsCount(commentsCount)
                .viewsCount(post.getViewsCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .subscribed(isFollowing)
                .isIsLiked(isLiked)
                .author(user == null ? null : PostResponseDto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .followers(followersCount)
                        .profilePictureUrl(user.getProfilePicture())
                        .name(user.getName())
                        .build());

        return builder.build();
    }

    public PostResponseDto convertToResponseDto2(Post post, Long currentUserId) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        Long followersCount = subscriptionRepository.countByBloggerId(post.getUserId());
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
                .viewsCount(post.getViewsCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .subscribed(isFollowing)
                .author(user == null ? null : PostResponseDto.UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .followers(followersCount)
                        .profilePictureUrl(user.getProfilePicture())
                        .name(user.getName())
                        .build());

        if (currentUserId != null) {
            boolean isBookmarked = bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            builder.isBookmarked(isBookmarked);
        } else {
            builder.isBookmarked(false);
        }
        return builder.build();
    }

    public Page<PostResponseDto> getFeedForUser(Long userId, int page, int limit, String category, String search, Boolean featured, String status) {

        boolean hasFilter = (category != null || search != null
                || featured != null );

        if (hasFilter) {
            return getAllPosts(page, limit, category,
                    status != null ? status : "PUBLISHED",
                    null, search, featured, "createdAt", "desc", userId);
        }

        List<FeedEntity> entries = cacheService.getFeed(userId, page);

        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        List<FeedEntity> freshEntries = entries.stream()
                .filter(e -> e.getCreatedAt().isAfter(cutoff))
                .toList();

        if (freshEntries.isEmpty()) {
            return getAllPosts(page, limit, null, "PUBLISHED", null,
                    null, null, "createdAt", "desc", userId);
        }

        List<Long> postIds = freshEntries.stream()
                .map(FeedEntity::getPostId)
                .toList();

        Pageable pageable = PageRequest.of(page, limit);
        Page<Post> feedPosts = postRepository
                .findByIdInOrderByCreatedAtDesc(postIds, pageable);

        int feedCount = (int) feedPosts.getTotalElements();
        if (feedCount < limit) {
            int remaining = limit - feedCount;

            List<Long> allExcluded = new ArrayList<>(postIds);

            Page<Post> publicPosts = postRepository.findPublicPostsExcluding(
                    allExcluded,
                    Post.PostStatus.PUBLISHED,
                    PageRequest.of(0, remaining,
                            Sort.by("createdAt").descending())
            );

            List<PostResponseDto> merged = new ArrayList<>();
            feedPosts.forEach(p -> merged.add(convertToResponseDto2(p, userId)));
            publicPosts.forEach(p -> merged.add(convertToResponseDto2(p, userId)));

            return new PageImpl<>(merged, pageable, merged.size());
        }

        return feedPosts.map(post -> convertToResponseDto2(post, userId));
    }


}
