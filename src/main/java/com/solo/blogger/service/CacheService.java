package com.solo.blogger.service;

import com.solo.blogger.entity.FeedEntity;
import com.solo.blogger.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final FeedRepository feedRepository;

    /**
     * Returns page N of a user's feed.
     * Result is cached in Caffeine — DB is only hit on a cold miss.
     */
    @Cacheable(value = "feeds", key = "#userId + '-' + #page")
    public List<FeedEntity> getFeed(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        return feedRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();
    }

    /**
     * Evicts only page 0 (the most recent page — the one users see first).
     * Called after every fan-out write.
     */
    @CacheEvict(value = "feeds", key = "#userId + '-0'")
    public void evictFeed(Long userId) {
        // Spring handles eviction via the annotation
    }

    /**
     * Evicts ALL pages of a user's feed.
     * Use this when a post is deleted or a user unfollows.
     */
    @CacheEvict(value = "feeds", allEntries = true)
    public void evictAllFeeds() {

    }
}
