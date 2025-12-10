package com.solo.blogger.repository;

import com.solo.blogger.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;


@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    Optional<Subscription> findByBloggerIdAndSubscriberId(Long bloggerId,Long subscriberId);

    @Query("Select s.subscriberId from Subscription s where s.bloggerId = :bloggerId")
    List<Long> findByBloggerId(Long bloggerId);
}
