package com.example.demo.service;

import com.google.firebase.auth.FirebaseToken;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record SyncResult(User user, boolean created) {}

    public SyncResult sync(String firebaseUid, FirebaseToken decoded) {
        Optional<User> existing = userRepository.findByFirebaseUid(firebaseUid);
        if (existing.isPresent()) {
            return new SyncResult(existing.get(), false);
        }

        User user = new User();
        user.setFirebaseUid(firebaseUid);
        if (decoded != null) {
            user.setEmail(decoded.getEmail());
            user.setUsername(decoded.getName() != null ? decoded.getName() : decoded.getEmail());
        }

        return new SyncResult(userRepository.save(user), true);
    }
}
