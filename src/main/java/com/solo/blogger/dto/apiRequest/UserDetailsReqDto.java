package com.solo.blogger.dto.apiRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDetailsReqDto {
    private String username;
    private String name;
    private String email;
    private String profilePicUrl;
    private String bio;
}
