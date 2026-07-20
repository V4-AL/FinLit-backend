package com.example.demo.controller;

import com.example.demo.model.Subscription;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserService currentUserService;

    public SubscriptionController(SubscriptionService subscriptionService,
                                   CurrentUserService currentUserService) {
        this.subscriptionService = subscriptionService;
        this.currentUserService = currentUserService;
    }

    // Initialize Paystack payment → returns payment URL
    @PostMapping("/initialize")
    public Map<String, Object> initializePayment() {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.initializePayment(userId);
    }

    // Paystack webhook — called automatically after payment, secured via HMAC signature
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("x-paystack-signature") String signature) {
        subscriptionService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    // Check module access for the authenticated user
    @GetMapping("/access/module/{moduleId}")
    public Map<String, Object> checkAccess(@PathVariable Long moduleId) {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.checkModuleAccess(userId, moduleId);
    }

    // Use a life when attempting a module
    @PostMapping("/use-life/module/{moduleId}")
    public Map<String, Object> useLife(@PathVariable Long moduleId) {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.useLife(userId, moduleId);
    }

    // Buy a life with points
    @PostMapping("/buy-life/module/{moduleId}")
    public Map<String, Object> buyLife(@PathVariable Long moduleId) {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.buyLife(userId, moduleId);
    }

    // Buy a quiz hint with points
    @PostMapping("/buy-hint")
    public Map<String, Object> buyHint() {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.buyHint(userId);
    }

    // Get subscription status for the authenticated user
    @GetMapping("/status")
    public Optional<Subscription> getStatus() {
        Long userId = currentUserService.getCurrentUser().getId();
        return subscriptionService.getSubscription(userId);
    }
}
