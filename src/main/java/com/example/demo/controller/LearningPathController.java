package com.example.demo.controller;

import com.example.demo.model.GoalType;
import com.example.demo.model.User;
import com.example.demo.service.LearningPathService;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    // Set goals for a user on signup
    @PostMapping("/goals/{userId}")
    public User setGoals(
            @PathVariable Long userId,
            @RequestBody List<String> goals) {
        return learningPathService.setUserGoals(userId, goals);
    }

    // Get recommended module titles for a user
    @GetMapping("/{userId}")
    public List<String> getLearningPath(@PathVariable Long userId) {
        return learningPathService.getLearningPath(userId);
    }

    // Get full learning path with user details
    @GetMapping("/{userId}/details")
    public Map<String, Object> getLearningPathDetails(@PathVariable Long userId) {
        return learningPathService.getLearningPathWithDetails(userId);
    }

    // Get all available goals (useful for frontend signup form)
    @GetMapping("/available-goals")
    public List<String> getAvailableGoals() {
        return Arrays.stream(GoalType.values())
                .map(Enum::name)
                .toList();
    }
}