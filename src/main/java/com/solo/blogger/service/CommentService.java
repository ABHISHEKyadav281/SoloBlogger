package com.solo.blogger.service;

import com.solo.blogger.dto.apiRequest.CommentDto;
import com.solo.blogger.dto.apiResponse.CommentResponseDto;
import com.solo.blogger.dto.apiResponse.UserDetailsDto;
import com.solo.blogger.entity.Comment;
import com.solo.blogger.entity.Post;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.CommentRepository;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public String addComment(CommentDto commentDto, Long userId) {

        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + commentDto.getPostId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (commentDto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentRepository.save(parentComment);
        }

        Comment comment = Comment.builder()
                .userId(userId)
                .postId(commentDto.getPostId())
                .content(commentDto.getContent())
                .parentId(commentDto.getParentId())
                .replyCount(0)
                .build();

        postRepository.save(post);

        commentRepository.save(comment);

        return "Comment added successfully";
    }

    public List<CommentResponseDto> getCommentsForPost(Long postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        List<Comment> comments = commentRepository.findTopLevelCommentsByPostId(postId);

        return comments.stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .repliesCount(comment.getReplyCount())
                        .createdAt(comment.getCreatedAt())
                        .isLiked(false)
                        .author(UserDetailsDto.builder()
                                .id(comment.getUser().getId())
                                .username(comment.getUser().getUsername())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Comment> getRepliesForComments(Long parentId) {
        List<Comment> comments = commentRepository.findCommentsByParentId(parentId);
        return comments;
    }

}
