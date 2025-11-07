package com.solo.blogger.service;

import com.solo.blogger.dto.CommentDto;
import com.solo.blogger.model.Comment;
import com.solo.blogger.model.Post;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.CommentRepository;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public Comment addComment(CommentDto commentDto,Long userId) {

        Post post = postRepository.findById(commentDto.getPost_id())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + commentDto.getPost_id()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: "));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(commentDto.getContent())
                .build();

        post.setCommentsCount(post.getCommentsCount() + 1);

        Comment savedComment = commentRepository.save(comment);
        postRepository.save(post);

        return savedComment;
    }

//    public Comment replyComment(CommentDto commentDto) {
//
//        Post post = postRepository.findById(commentDto.getPost_id())
//                .orElseThrow(() -> new RuntimeException("Post not found with id: " + commentDto.getPost_id()));
//
//        User user = userRepository.findById(commentDto.getUser_id())
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + commentDto.getUser_id()));
//
//        Comment parentComment = commentRepository.findById(commentDto.getParent_comment_id())
//                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentDto.getParent_comment_id()));
//
//        Comment reply = Comment.builder()
//                .user(user)
//                .post(post)
//                .content(commentDto.getContent())
//                .createdAt(new Date())
//                .build();
//
//        Comment savedReply = commentRepository.save(reply);
//
//        parentComment.getReplies().add(savedReply);
//
//        commentRepository.save(parentComment);
//
//        return savedReply;
//    }

//    public void deleteComment(CommentDto commentDto){
//        User user=userRepository.findById(commentDto.getUser_id())
//                .orElseThrow(()->new RuntimeException("user not found"));
//        Post post = postRepository.findById(commentDto.getPost_id())
//                .orElseThrow(() -> new RuntimeException("Post not found with id: " + commentDto.getPost_id()));
//
//        Comment comment=commentRepository.findById(commentDto.getId())
//                .orElseThrow(()->new RuntimeException("not comment present"));
//
//        if(user.getId()==commentDto.getUser_id() || post.getUser().getId()==commentDto.getUser_id()) {
//            commentRepository.delete(comment);
//        }
//        else{
//            throw new RuntimeException("You are not authorized to delete this comment!");
//        }
//    }


}
