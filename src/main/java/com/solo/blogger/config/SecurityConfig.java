package com.solo.blogger.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for testing (only for development)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/subscription/v1/**").permitAll()
                        .requestMatchers("/post/v1/**").permitAll()
                        .requestMatchers("comment/v1/**").permitAll()
                        .requestMatchers("/auth/v1/signup", "/auth/v1/signin").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Ensure stateless API
                .httpBasic(httpBasic -> httpBasic.disable())  // Disable basic auth
                .formLogin(form -> form.disable());  // Disable form login

        return http.build();
    }
}