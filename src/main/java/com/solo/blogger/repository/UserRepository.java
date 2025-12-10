package com.solo.blogger.repository;

import com.solo.blogger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findById(long id);

    @Query("select u.username from User u where u.id In :ids")
    List<String> findUsersById(List<Long> ids);
}
