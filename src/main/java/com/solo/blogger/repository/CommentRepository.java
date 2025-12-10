package com.solo.blogger.repository;

import com.solo.blogger.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment,String > {

    Optional<Comment> findById(long id);
}
