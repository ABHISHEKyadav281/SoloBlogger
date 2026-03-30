package com.solo.blogger.dto.apiResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsDto {

    private Long id;
    private String username;
    private String profileImage;
    private String email;
    private String name;
    private String bio;
    private Long followers;
    private Long following;
    private Long posts;
    private Long totalLikes;
    private boolean isSubscribed;
    private String profilePictureUrl;
}
