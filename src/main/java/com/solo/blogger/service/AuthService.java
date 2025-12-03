package com.solo.blogger.service;

import com.solo.blogger.dto.responseFactory.SuccessResponse;
import com.solo.blogger.utils.JwtUtil;
import com.solo.blogger.dto.apiResponse.AuthRequest;
import com.solo.blogger.entity.User;
import com.solo.blogger.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // ✅ Constructor-based dependency injection
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();  // ✅ Initializes BCrypt
    }

    public ResponseEntity<?> login (AuthRequest authRequest) {
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        SuccessResponse res= jwtUtil.generateToken(user.getUsername(),user.getId());
        return ResponseEntity.ok(res);
    }
}

