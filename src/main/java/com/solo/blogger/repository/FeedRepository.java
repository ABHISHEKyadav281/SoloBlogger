package com.solo.blogger.repository;

import com.solo.blogger.entity.FeedEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {

    Page<FeedEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Used when a post is deleted — clean up all feed entries for it
    @Modifying
    @Query("DELETE FROM FeedEntity f WHERE f.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // Used when a user unfollows — remove that author's posts from their feed
    @Modifying
    @Query("DELETE FROM FeedEntity f WHERE f.userId = :userId AND f.authorId = :authorId")
    void deleteByUserIdAndAuthorId(@Param("userId") Long userId,
                                   @Param("authorId") Long authorId);
}