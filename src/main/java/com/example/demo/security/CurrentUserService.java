package com.example.demo.security;

import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentFirebaseUid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof String uid)) {
            throw new UnauthorizedException("No authenticated user");
        }
        return uid;
    }

    public User getCurrentUser() {
        return userRepository.findByFirebaseUid(getCurrentFirebaseUid())
                .orElseThrow(() -> new UnauthorizedException(
                        "User not provisioned — call POST /api/auth/sync first"));
    }
}
