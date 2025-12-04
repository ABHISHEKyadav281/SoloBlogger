package com.solo.blogger.service;

import com.solo.blogger.entity.PostLike;
import com.solo.blogger.enums.ReactionType;
import com.solo.blogger.repository.PostLikeRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ReactionService {

    @Autowired
    private PostLikeRepository postLikeRepository;


    public void reactPost(Long userId, Long postId, ReactionType reactionType) {

        if (reactionType.equals(ReactionType.LIKE)) {
            postLikeRepository.save(PostLike.builder().userId(userId).postId(postId).build());
        } else {
            PostLike reaction = postLikeRepository.findByUserIdAndPostId(userId, postId).orElseThrow(() -> new RuntimeException("Reaction not found"));
            postLikeRepository.delete(reaction);
        }

    }

//    public void reactToComment(Long userId, Long commentId, ReactionType reactionType) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
//        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
//
//        Optional<Reaction> existingReaction = reactionRepository.findByUserAndComment(user, comment);
//
//        if (existingReaction.isPresent()) {
//            if (existingReaction.get().getReactionType().equals(reactionType)) {
//                reactionRepository.delete(existingReaction.get());
//            } else {
//                existingReaction.get().setReactionType(reactionType);
//                reactionRepository.save(existingReaction.get());
//            }
//        } else {
//            Reaction reaction = Reaction.builder()
//                    .user(user)
//                    .comment(comment)
//                    .reactionType(reactionType)
//                    .build();
//            reactionRepository.save(reaction);
//        }
//    }

}
