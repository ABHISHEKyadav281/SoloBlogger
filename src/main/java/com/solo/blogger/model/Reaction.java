//package com.solo.blogger.model;
//
//import com.solo.blogger.enums.ReactionType;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Reaction {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
//
//    @ManyToOne
//            @JoinColumn(name = "userId",nullable = false )
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "postId")
//    private Post post;
//
//    @ManyToOne
//    @JoinColumn(name = "commentId")
//    private Comment comment;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ReactionType reactionType;
//
//
//}
