package com.solo.blogger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "oauth_user")
    private boolean oauthUser;

    @Column(nullable = true)
    private String password;

}