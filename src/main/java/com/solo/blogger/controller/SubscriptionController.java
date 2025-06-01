package com.solo.blogger.controller;

import com.solo.blogger.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/subscription/v1")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/subscribe/{subscriberId}/{bloggerId}")
    public ResponseEntity<?> subscribe(@PathVariable Long subscriberId, @PathVariable Long bloggerId){
        System.out.println("hii");
        subscriptionService.subscribe(subscriberId,bloggerId);
        return ResponseEntity.ok("Subscribed succesfully");
    }

    @DeleteMapping("unSubscribe/{subscriberId}/{bloggerId}")
    public ResponseEntity<?> unSubscribe(@PathVariable Long subscriberId, @PathVariable Long bloggerId){
        subscriptionService.unSubscribe(subscriberId,bloggerId);
        return ResponseEntity.ok("unSubscribed successfully");
    }
}
