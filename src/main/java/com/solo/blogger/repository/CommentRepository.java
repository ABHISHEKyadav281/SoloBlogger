package com.solo.blogger.repository;

import com.solo.blogger.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,String > {

    Optional<Comment> findById(long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.postId = :postId AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.parentId = :parentId  ORDER BY c.createdAt ASC")
    List<Comment> findCommentsByParentId(@Param("parentId") Long parentId);

    Long countByPostId(Long postId);

}
