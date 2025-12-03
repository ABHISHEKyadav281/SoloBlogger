//package com.solo.blogger.repository;
//
//import com.solo.blogger.model.PostLike;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
//
//   boolean existsByUserIdAndPostId(Long userId, Long postId);
//
//   Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
//
//   @Query("SELECT COUNT(p) FROM PostLike p WHERE p.post.id = :postId")
//   long countByPostId(@Param("postId") Long postId);
//}