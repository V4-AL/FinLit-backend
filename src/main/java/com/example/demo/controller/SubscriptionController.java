package com.example.demo.controller;

import com.example.demo.model.Subscription;
import com.example.demo.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Initialize Paystack payment → returns payment URL
    @PostMapping("/initialize/{userId}")
    public Map<String, Object> initializePayment(@PathVariable Long userId) {
        return subscriptionService.initializePayment(userId);
    }

    // Paystack webhook — called automatically after payment
    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> payload) {
        String event = (String) payload.get("event");
        if ("charge.success".equals(event)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String reference = (String) data.get("reference");
            subscriptionService.handlePaymentSuccess(reference);
        }
    }

    // Check module access
    @GetMapping("/access/{userId}/module/{moduleId}")
    public Map<String, Object> checkAccess(
            @PathVariable Long userId,
            @PathVariable Long moduleId) {
        return subscriptionService.checkModuleAccess(userId, moduleId);
    }

    // Use a life when attempting a module
    @PostMapping("/use-life/{userId}/module/{moduleId}")
    public Map<String, Object> useLife(
            @PathVariable Long userId,
            @PathVariable Long moduleId) {
        return subscriptionService.useLife(userId, moduleId);
    }

    // Buy a life with points
    @PostMapping("/buy-life/{userId}/module/{moduleId}")
    public Map<String, Object> buyLife(
            @PathVariable Long userId,
            @PathVariable Long moduleId) {
        return subscriptionService.buyLife(userId, moduleId);
    }

    // Buy a quiz hint with points
    @PostMapping("/buy-hint/{userId}")
    public Map<String, Object> buyHint(@PathVariable Long userId) {
        return subscriptionService.buyHint(userId);
    }

    // Get subscription status
    @GetMapping("/status/{userId}")
    public Optional<Subscription> getStatus(@PathVariable Long userId) {
        return subscriptionService.getSubscription(userId);
    }
}