package com.solo.blogger.service;


import com.solo.blogger.dto.apiRequest.UserDetailsReqDto;
import com.solo.blogger.dto.apiRequest.UserDto;
import com.solo.blogger.dto.apiResponse.UserDetailsDto;
import com.solo.blogger.dto.responseFactory.SuccessResponse;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.SubscriptionRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SubscriptionRepository subscribedRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<?> register(UserDto userDto) {

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        userRepository.save(user);
        return ResponseEntity.ok().body(SuccessResponse.builder().statusCode("202").data("User Registered successfully").build());
    }

    public ResponseEntity<?> userDetails(Long bloggerId, Long userId) {
        User user = userRepository.findById(bloggerId).orElseThrow(() -> new RuntimeException("user not found"));
        List<Long> postIds = postRepository.postIdsByUserId(bloggerId);
        Long postCount = (long) postIds.size();
        Long followers = subscribedRepository.countByBloggerId(bloggerId);
        Long following = subscribedRepository.countBySubscriberId(bloggerId);
        boolean isFollowing = subscribedRepository.existsByBloggerIdAndSubscriberId(bloggerId, userId);
        Long totLikes = postRepository.countLikesForPostIds(postIds);
        UserDetailsDto userDetail = UserDetailsDto.builder().username(user.getUsername()).totalLikes(totLikes)
                .email(user.getEmail())
                .username(user.getUsername())
                .profileImage(user.getProfilePicture())
                .name(user.getName())
                .bio(user.getBio())
                .posts(postCount)
                .followers(followers)
                .following(following)
                .isSubscribed(isFollowing)
                .build();
        return ResponseEntity.ok().body(SuccessResponse.builder().statusCode("200").data(userDetail).build());
    }

    public ResponseEntity<?> modifyUserDetails(UserDetailsReqDto req, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setBio(req.getBio());
        user.setProfilePicture(req.getProfilePicUrl());
        userRepository.save(user);
        return ResponseEntity.ok().body(SuccessResponse.builder().statusCode("200").data("profile updated successfully").build());
    }

}
