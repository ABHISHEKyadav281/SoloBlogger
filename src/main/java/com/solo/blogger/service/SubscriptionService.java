package com.solo.blogger.service;


import com.solo.blogger.model.Subscription;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.SubscriptionRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public void  subscribe(Long subscriberId,Long bloggerId){
        if(subscriberId.equals(bloggerId)){
            throw new IllegalArgumentException("You can't subscribe yourself !");
        }

        User subscriber=userRepository.findById(subscriberId).orElseThrow(()->new RuntimeException("subscriber not found !"));
        User blogger=userRepository.findById(bloggerId).orElseThrow(()->new RuntimeException("blogger not found !"));

        if(subscriptionRepository.existsBySubscriberAndBlogger(subscriber,blogger)){
            throw new IllegalStateException("Already subscribed");
        }

        Subscription subscription= Subscription.builder().subscriber(subscriber)
                .blogger(blogger).build();
        subscriptionRepository.save(subscription);
    }

    public void unSubscribe(Long subscriberId, Long bloggerId){
        if(subscriberId.equals(bloggerId)){
            throw new IllegalArgumentException("You can unfollow yourself");
        }
        User subscriber=userRepository.findById(subscriberId).orElseThrow(()->new RuntimeException("subscriber not found !"));
        User blogger=userRepository.findById(bloggerId).orElseThrow(()->new RuntimeException("blogger not found !"));

        Subscription subscription=subscriptionRepository.findBySubscriberAndBlogger(subscriber,blogger).orElseThrow(()->new RuntimeException("You are not subscribed to this blogger"));
        subscriptionRepository.delete(subscription);
    }



}
