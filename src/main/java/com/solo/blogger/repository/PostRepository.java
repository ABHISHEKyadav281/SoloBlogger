package com.solo.blogger.repository;

import com.solo.blogger.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {

    Optional<Post> findById(long id);
    List<Post> findByUserId(long user_id);
}
