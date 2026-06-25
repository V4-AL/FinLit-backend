package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by ID
    public User getUserById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Get a user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Create or update a user
    public User saveUser(@NonNull User user) {
        return userRepository.save(user);
    }

    // Delete a user by ID
    public void deleteUser(@NonNull Long id) {
        userRepository.deleteById(id);
    }
}