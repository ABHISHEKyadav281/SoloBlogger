package com.solo.blogger.repository;

import com.solo.blogger.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {
    @Query("SELECT b.postId FROM Bookmark b WHERE b.userId = :userId")
    List<Long> findByUserId(Long userId);

    Bookmark findByUserIdAndPostId(Long userId,Long postId);
}
