package com.solo.blogger.service;

import com.solo.blogger.dto.GetPostDto;
import com.solo.blogger.dto.PostDto;
import com.solo.blogger.model.Post;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public Post addPost(PostDto postDto){
        System.out.println("add post service called");
        User user = userRepository.findById(postDto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + postDto.getUser_id()));

        Post post=Post.builder()
                .user(user)
                .Title(postDto.getTitle())
                .Content(postDto.getContent())
                .Category(postDto.getCategory())
                .Tags(postDto.getTags())
                .picture(postDto.getPicture())
                .createdAt(new Date())
                .build();

        return postRepository.save(post);
    }

    public List<PostDto> getAllPost(){
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(post -> new PostDto(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getPicture(),
                post.getTags(),
                post.getCreatedAt()
        )).collect(Collectors.toList());
    }

    public PostDto PostById(GetPostDto getPostDto){
        Post post = postRepository.findById(getPostDto.getId()).orElseThrow(()->new RuntimeException("Post not found"));
        System.out.println(getPostDto.getId());
        System.out.println(post);
        return  new PostDto(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getPicture(),
                post.getTags(),
                post.getCreatedAt()
        );
    }

    public List<PostDto> PostByUserId(GetPostDto getPostDto){
        List<Post> posts = postRepository.findByUserId(getPostDto.getUser_id());
        System.out.println(getPostDto.getId());
        System.out.println(posts);
        return posts.stream().map(post -> new PostDto(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getPicture(),
                post.getTags(),
                post.getCreatedAt()
        )).collect(Collectors.toList());
    }

    public void deletePost(GetPostDto getPostDto){

        User user=userRepository.findById(getPostDto.getUser_id()).orElseThrow(()->new RuntimeException("user not found"));

        if(user.getId()!=getPostDto.getUser_id()) throw new RuntimeException("you are not authorize to delete this post");
        Post post=postRepository.findById(getPostDto.getId()).orElseThrow(()->new RuntimeException("Post does not exist"));
        postRepository.delete(post);
    }


}
