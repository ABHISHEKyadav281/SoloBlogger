//package com.solo.blogger.repository;
//
//import com.solo.blogger.model.Subscription;
//import com.solo.blogger.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
//
//    Optional<Subscription> findBySubscriberAndBlogger(User subscriber,User blogger);
//
//    List<Subscription> findBySubscriber(User subscriber);
//
//    List<Subscription> findByBlogger(User blogger);
//
//    boolean existsBySubscriberAndBlogger(User subscriber,User blogger);
//
//}
