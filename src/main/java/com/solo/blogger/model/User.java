package com.solo.blogger.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name="Users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "id",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "id",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "subscriber",cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<Subscription> subscriptions;

    @OneToMany(mappedBy = "blogger",cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<Subscription> subscribers;


}