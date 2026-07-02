package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubscriptionService {

    private static final int HINT_COST = 20;
    private static final int LIFE_COST = 30;
    private static final int FREE_LIVES_PER_MODULE = 3;
    private static final long FREE_MODULE_ID = 1L;

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Value("${paystack.base.url}")
    private String paystackBaseUrl;

    @Value("${subscription.price.kobo}")
    private int subscriptionPriceKobo;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ModuleLifeRepository moduleLifeRepository;
    private final RestTemplate restTemplate;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                                UserRepository userRepository,
                                ModuleLifeRepository moduleLifeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.moduleLifeRepository = moduleLifeRepository;
        this.restTemplate = new RestTemplate();
    }

    // Initialize Paystack payment
    public Map<String, Object> initializePayment(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("email", user.getEmail());
        body.put("amount", subscriptionPriceKobo);
        body.put("currency", "GHS");
        body.put("metadata", Map.of("userId", userId));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                paystackBaseUrl + "/transaction/initialize",
                entity,
                Map.class
        );

        Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
        return responseData;
    }

    // Called by Paystack webhook after successful payment
    public void handlePaymentSuccess(String reference) {
        // Verify transaction with Paystack
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                paystackBaseUrl + "/transaction/verify/" + reference,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        String status = (String) data.get("status");

        if (!"success".equals(status)) {
            throw new RuntimeException("Payment verification failed");
        }

        // Get userId from metadata
        Map<String, Object> metadata = (Map<String, Object>) data.get("metadata");
        Long userId = Long.valueOf(metadata.get("userId").toString());

        // Activate subscription
        Subscription subscription = subscriptionRepository
                .findByUserId(userId)
                .orElse(new Subscription());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        subscription.setUser(user);
        subscription.setStatus(SubscriptionStatus.PREMIUM);
        subscription.setPaystackReference(reference);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setExpiryDate(LocalDateTime.now().plusMonths(1));
        subscription.setLastRenewalDate(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }

    // Check if user can access a module
    public Map<String, Object> checkModuleAccess(Long userId, Long moduleId) {
        Map<String, Object> result = new HashMap<>();

        // Module 1 is always free
        if (moduleId.equals(FREE_MODULE_ID)) {
            result.put("hasAccess", true);
            result.put("reason", "FREE_MODULE");
            return result;
        }

        // Check subscription
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);
        boolean isPremium = subscription.isPresent() &&
                subscription.get().getStatus() == SubscriptionStatus.PREMIUM &&
                subscription.get().getExpiryDate().isAfter(LocalDateTime.now());

        if (!isPremium) {
            result.put("hasAccess", false);
            result.put("reason", "UPGRADE_REQUIRED");
            result.put("message", "Subscribe for GHS 20/month to access all modules");
            return result;
        }

        // Check lives
        ModuleLife moduleLife = moduleLifeRepository
                .findByUserIdAndModuleId(userId, moduleId)
                .orElse(null);

        int livesRemaining = moduleLife == null ? FREE_LIVES_PER_MODULE
                : moduleLife.getLivesRemaining();

        if (livesRemaining <= 0) {
            result.put("hasAccess", false);
            result.put("reason", "NO_LIVES");
            result.put("message", "You have no lives remaining. Spend 30 points to buy a life.");
            result.put("livesRemaining", 0);
            return result;
        }

        result.put("hasAccess", true);
        result.put("livesRemaining", livesRemaining);
        result.put("reason", "PREMIUM_ACCESS");
        return result;
    }

    // Deduct a life when user attempts a module
    public Map<String, Object> useLife(Long userId, Long moduleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ModuleLife moduleLife = moduleLifeRepository
                .findByUserIdAndModuleId(userId, moduleId)
                .orElseGet(() -> {
                    ModuleLife newLife = new ModuleLife();
                    newLife.setUser(user);
                    newLife.setModuleId(moduleId);
                    newLife.setLivesRemaining(FREE_LIVES_PER_MODULE);
                    return newLife;
                });

        if (moduleLife.getLivesRemaining() <= 0) {
            throw new RuntimeException("No lives remaining for this module");
        }

        moduleLife.setLivesRemaining(moduleLife.getLivesRemaining() - 1);
        moduleLife.setLastUpdated(LocalDateTime.now());
        moduleLifeRepository.save(moduleLife);

        Map<String, Object> result = new HashMap<>();
        result.put("livesRemaining", moduleLife.getLivesRemaining());
        result.put("moduleId", moduleId);
        return result;
    }

    // Buy a life with points
    public Map<String, Object> buyLife(Long userId, Long moduleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAvailablePoints() < LIFE_COST) {
            throw new RuntimeException("Not enough points. Need " + LIFE_COST +
                    " points, you have " + user.getAvailablePoints());
        }

        user.setSpentPoints(user.getSpentPoints() + LIFE_COST);
        userRepository.save(user);

        ModuleLife moduleLife = moduleLifeRepository
                .findByUserIdAndModuleId(userId, moduleId)
                .orElseGet(() -> {
                    ModuleLife newLife = new ModuleLife();
                    newLife.setUser(user);
                    newLife.setModuleId(moduleId);
                    newLife.setLivesRemaining(0);
                    return newLife;
                });

        moduleLife.setLivesRemaining(moduleLife.getLivesRemaining() + 1);
        moduleLife.setLastUpdated(LocalDateTime.now());
        moduleLifeRepository.save(moduleLife);

        Map<String, Object> result = new HashMap<>();
        result.put("livesRemaining", moduleLife.getLivesRemaining());
        result.put("pointsSpent", LIFE_COST);
        result.put("availablePoints", user.getAvailablePoints());
        return result;
    }

    // Buy a quiz hint with points
    public Map<String, Object> buyHint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAvailablePoints() < HINT_COST) {
            throw new RuntimeException("Not enough points. Need " + HINT_COST +
                    " points, you have " + user.getAvailablePoints());
        }

        user.setSpentPoints(user.getSpentPoints() + HINT_COST);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("hintGranted", true);
        result.put("pointsSpent", HINT_COST);
        result.put("availablePoints", user.getAvailablePoints());
        return result;
    }

    public Optional<Subscription> getSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}