package com.example.demo.config;

import com.example.demo.security.FirebaseTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
        this.firebaseTokenFilter = firebaseTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no Firebase token needed
                .requestMatchers("/api/learning-path/available-goals").permitAll()
                .requestMatchers("/api/lessons").permitAll()
                .requestMatchers("/api/lessons/**").permitAll()
                .requestMatchers("/api/modules").permitAll()
                .requestMatchers("/api/modules/**").permitAll()
                .requestMatchers("/api/simulations/**").permitAll()
                .requestMatchers("/api/subscription/webhook").permitAll()
                // Everything else requires a valid Firebase token
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseTokenFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}