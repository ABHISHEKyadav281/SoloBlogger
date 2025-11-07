package com.solo.blogger.repository;

import com.solo.blogger.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find by status
    Page<Post> findByStatus(Post.PostStatus status, Pageable pageable);

    // Find by category
    Page<Post> findByCategory(String category, Pageable pageable);

    // Find featured posts
    Page<Post> findByFeaturedTrue(Pageable pageable);

    // Find featured posts with specific status
    Page<Post> findByFeaturedTrueAndStatus(Post.PostStatus status, Pageable pageable);

    // Find by category and featured
    Page<Post> findByCategoryAndFeatured(String category, Boolean featured, Pageable pageable);

    // Find by user ID
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // Search posts by title or content
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Post> searchPosts(@Param("search") String search, Pageable pageable);

    // Count posts by user
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // Find posts by tag
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t = :tag")
    Page<Post> findByTag(@Param("tag") String tag, Pageable pageable);
}



//package com.solo.blogger.repository;
//
//import com.solo.blogger.model.Post;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface PostRepository extends JpaRepository<Post,String> {
//
//    Optional<Post> findById(long id);
//    List<Post> findByUserId(long user_id);
//}
