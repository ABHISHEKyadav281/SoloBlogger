package com.solo.blogger.service;

import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.entity.Bookmark;
import com.solo.blogger.entity.Post;
import com.solo.blogger.entity.Subscription;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.BookmarkRepository;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserActionService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    public void bookmarkPost(Long postId, Long userId){
        Bookmark bookmark = Bookmark.builder().postId(postId).userId(userId).build();
        bookmarkRepository.save(bookmark);
    }

    public List<PostResponseDto> getBookmarkedPosts(Long userId) {
        List<Long> postIds = bookmarkRepository.findByUserId(userId);
        List<Post> posts = postRepository.findAllById(postIds);
        return posts.stream()
                .map(post -> {
                    PostResponseDto dto = postService.convertToResponseDto2(post, null);
                    dto.setBookmarked(true);
                    return dto;
                })
                .toList();
    }

    public void unBookmarkPost(Long postId, Long userId){
        Bookmark post=bookmarkRepository.findByUserIdAndPostId(userId,postId);
        bookmarkRepository.delete(post);
    }

    public void subscribeBlogger(Long bloggerId, Long userId){
        Subscription subscriber=Subscription.builder().bloggerId(bloggerId).subscriberId(userId).build();
        subscriptionRepository.save(subscriber);
    }

    public void unSubscribeBlogger(Long bloggerId, Long userId){
        Subscription subscriber=subscriptionRepository.findByBloggerIdAndSubscriberId(bloggerId,userId).orElseThrow(()->new RuntimeException("you didn't subscribed this blogger"));
        subscriptionRepository.delete(subscriber);
    }

    public List<String> subscribersList( Long userId){
        List<Long> subscriberId=subscriptionRepository.findByBloggerId(userId);
        return userRepository.findUsersById(subscriberId);
    }

    public Long subscribersCount( Long userId){
        return subscriptionRepository.countByBloggerId(userId);
    }

    public boolean isSubscribed(Long bloggerId, Long userId){
        Optional<Subscription> subscriber=subscriptionRepository.findByBloggerIdAndSubscriberId(bloggerId,userId);
        return subscriber.isPresent();
    }

}
