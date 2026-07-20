package com.example.demo.controller;

import com.google.firebase.auth.FirebaseToken;
import com.example.demo.model.User;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    // Idempotent find-or-create for the authenticated Firebase identity.
    // Call once right after Firebase sign-up/sign-in; safe to call again on every app launch.
    @PostMapping("/sync")
    public ResponseEntity<User> sync() {
        String firebaseUid = currentUserService.getCurrentFirebaseUid();
        FirebaseToken decoded = currentDecodedToken();

        AuthService.SyncResult result = authService.sync(firebaseUid, decoded);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.user());
    }

    @GetMapping("/me")
    public User me() {
        return currentUserService.getCurrentUser();
    }

    private FirebaseToken currentDecodedToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getDetails() instanceof FirebaseToken token ? token : null;
    }
}
