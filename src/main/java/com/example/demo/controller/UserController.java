package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Called after Firebase login to register user in your DB
    @PostMapping("/register")
    public User registerUser(@RequestBody User user,
                             Authentication authentication) {
        // Use Firebase UID as a check to avoid duplicates
        String firebaseUid = (String) authentication.getPrincipal();

        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            return existing;
        }

        user.setEmail(user.getEmail());
        user.setUsername(user.getUsername());
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}