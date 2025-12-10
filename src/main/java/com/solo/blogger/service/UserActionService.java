package com.solo.blogger.service;

import com.solo.blogger.dto.apiResponse.PostResponseDto;
import com.solo.blogger.entity.Bookmark;
import com.solo.blogger.entity.Post;
import com.solo.blogger.repository.BookmarkRepository;
import com.solo.blogger.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActionService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    public void bookmarkPost(Long postId, Long userId){
        Bookmark bookmark = Bookmark.builder().postId(postId).userId(userId).build();
        bookmarkRepository.save(bookmark);
    }

    public List<PostResponseDto> getBookmarkedPosts(Long userId) {
        List<Long> postIds = bookmarkRepository.findByUserId(userId);
        System.out.println(postIds.size());
        List<Post> posts = postRepository.findAllById(postIds);
        System.out.println(posts.size());
        return posts.stream().map(postService::convertToResponseDto).toList();
    }
}
