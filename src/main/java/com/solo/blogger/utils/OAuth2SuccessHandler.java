package com.solo.blogger.utils;

import com.solo.blogger.entity.User;
import com.solo.blogger.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email   = oAuth2User.getAttribute("email");
        String name    = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // ✅ find user by email, or create if first time Google login
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(generateUniqueUsername(email));
                    newUser.setProfilePicture(picture);
                    newUser.setPassword(ThreadLocalRandom.current().nextInt(1000, 9999) + "");
                    newUser.setOauthUser(true);

                    User saved = userRepository.save(newUser);

                    return saved;
                });

        // ✅ now userId is available
        String token = jwtUtil.generateOAuthToken(
                email, user.getUsername(), picture, user.getId()
        );
//        System.out.println("token : "+token);
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/oauth2/callback?token=" + token);
    }

    private String generateUniqueUsername(String email) {
        // take part before @ e.g. "john.doe" from "john.doe@gmail.com"
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "");  // remove special chars

        // if username already exists, append random 4-digit number
        String username = base;
        while (userRepository.existsByUsername(username)) {
            username = base + ThreadLocalRandom.current().nextInt(1000, 9999);
        }
        return username;
    }
}