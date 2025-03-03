package com.solo.blogger.service;

import com.solo.blogger.enums.ReactionType;
import com.solo.blogger.model.Comment;
import com.solo.blogger.model.Post;
import com.solo.blogger.model.Reaction;
import com.solo.blogger.model.User;
import com.solo.blogger.repository.CommentRepository;
import com.solo.blogger.repository.PostRepository;
import com.solo.blogger.repository.ReactionRepository;
import com.solo.blogger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;


    public void reactPost(Long userId, Long postId, ReactionType reactionType) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Reaction> existingReaction = reactionRepository.findByUserAndPost(user, post);

        if (existingReaction.isPresent()) {
            if (existingReaction.get().getReactionType().equals(reactionType)) {
                reactionRepository.delete(existingReaction.get());
            } else {
                existingReaction.get().setReactionType(reactionType);
                reactionRepository.save(existingReaction.get());
            }
        } else {
            Reaction reaction = Reaction.builder()
                    .user(user)
                    .post(post)
                    .reactionType(reactionType)
                    .build();
            reactionRepository.save(reaction);

        }
    }

    public void reactToComment(Long userId, Long commentId, ReactionType reactionType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));

        Optional<Reaction> existingReaction = reactionRepository.findByUserAndComment(user, comment);

        if (existingReaction.isPresent()) {
            if (existingReaction.get().getReactionType().equals(reactionType)) {
                reactionRepository.delete(existingReaction.get());
            } else {
                existingReaction.get().setReactionType(reactionType);
                reactionRepository.save(existingReaction.get());
            }
        } else {
            Reaction reaction = Reaction.builder()
                    .user(user)
                    .comment(comment)
                    .reactionType(reactionType)
                    .build();
            reactionRepository.save(reaction);
        }
    }

}
