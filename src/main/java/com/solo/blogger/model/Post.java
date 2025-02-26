package com.solo.blogger.model;

import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.Type;
import java.util.*;


@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    String Title;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    private Date createdAt;;

    private long commentsCount;

    private String picture;

    private String Category;

    private List<String> Tags;

    @Column(columnDefinition = "TEXT",nullable = false)
    String Content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Comment> comments;

//    private List<User> likes;
//
//    private List<User> dislikes;



}
