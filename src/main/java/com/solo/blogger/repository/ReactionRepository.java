package com.solo.blogger.repository;

import com.solo.blogger.model.Comment;
import com.solo.blogger.model.Post;
import com.solo.blogger.model.Reaction;
import com.solo.blogger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction,String> {
    Optional<Reaction> findByUserAndPost(User userId, Post postId);
    Optional<Reaction> findByUserAndComment(User userId, Comment commentId);
}
