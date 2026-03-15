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

    /**
     * Entry point — called from PostService after a post is saved.
     * Loads all follower IDs and dispatches one async task per follower.
     */
    public void fanOut(Post post) {
        // Get all subscriber IDs for this blogger
        // Matches your existing SubscriptionRepository pattern
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

    /**
     * Runs on a separate thread from ThreadPoolTaskExecutor.
     * Writes one FeedEntry row and evicts that follower's cache.
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> writeToFeedAsync(Long followerId, Post post) {
        try {
            // Guard: skip if already written (e.g. retry scenario)
            if (feedRepository.existsByUserIdAndPostId(followerId, post.getId())) {
                return CompletableFuture.completedFuture(null);
            }

            FeedEntity entry = FeedEntity.builder()
                    .userId(followerId)
                    .postId(post.getId())
                    .authorId(post.getUserId())
                    .build();

            feedRepository.save(entry);

            // Evict page 0 of this follower's feed cache
            cacheService.evictFeed(followerId);

            log.debug("Feed entry written for follower {} post {}", followerId, post.getId());

        } catch (Exception e) {
            // Never let one follower's failure break the whole fan-out
            log.error("Failed to write feed entry for follower {}: {}",
                    followerId, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when a user unfollows a blogger.
     * Removes that blogger's posts from the unfollower's feed.
     */
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

    /**
     * Called when a post is deleted.
     * Removes it from every follower's feed.
     */
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