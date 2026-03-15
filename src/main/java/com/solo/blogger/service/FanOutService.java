package com.solo.blogger.service;

import com.solo.blogger.entity.FeedEntity;
import com.solo.blogger.entity.Post;
import com.solo.blogger.repository.FeedRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FanOutService {

    private final FeedRepository feedRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CacheService cacheService;
    private final NotificationService notificationService;

    public void fanOut(Post post) {
        List<Long> followerIds = subscriptionRepository
                .findSubscriberIdByBloggerId(post.getUserId());

        if (followerIds.isEmpty()) {
            log.info("No followers for user {}, skipping fan-out", post.getUserId());
            return;
        }

        log.info("Fan-out started for post {} to {} followers",
                post.getId(), followerIds.size());

        for (Long followerId : followerIds) {
            writeToFeedAsync(followerId, post);
        }
    }


    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> writeToFeedAsync(Long followerId, Post post) {
        try {
            if (feedRepository.existsByUserIdAndPostId(followerId, post.getId())) {
                return CompletableFuture.completedFuture(null);
            }

            FeedEntity entry = FeedEntity.builder()
                    .userId(followerId)
                    .postId(post.getId())
                    .authorId(post.getUserId())
                    .build();

            feedRepository.save(entry);

            cacheService.evictFeed(followerId);

            log.debug("Feed entry written for follower {} post {}", followerId, post.getId());

        } catch (Exception e) {
            log.error("Failed to write feed entry for follower {}: {}",
                    followerId, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }


    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> removeFeedOnUnfollow(Long userId, Long authorId) {
        try {
            feedRepository.deleteByUserIdAndAuthorId(userId, authorId);
            cacheService.evictFeed(userId);
            log.info("Feed cleaned for user {} after unfollowing author {}", userId, authorId);
        } catch (Exception e) {
            log.error("Failed to clean feed on unfollow for user {}: {}",
                    userId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }


    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> removePostFromAllFeeds(Long postId) {
        try {
            feedRepository.deleteByPostId(postId);
            log.info("Removed post {} from all feeds", postId);
        } catch (Exception e) {
            log.error("Failed to remove post {} from feeds: {}", postId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}