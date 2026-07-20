package com.example.demo.controller;

import com.example.demo.model.GoalType;
import com.example.demo.model.User;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.LearningPathService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-path")
@Validated
public class LearningPathController {

    private final LearningPathService learningPathService;
    private final CurrentUserService currentUserService;

    public LearningPathController(LearningPathService learningPathService,
                                   CurrentUserService currentUserService) {
        this.learningPathService = learningPathService;
        this.currentUserService = currentUserService;
    }

    // Set goals for the authenticated user
    @PostMapping("/goals")
    public User setGoals(@RequestBody @NotEmpty List<String> goals) {
        Long userId = currentUserService.getCurrentUser().getId();
        return learningPathService.setUserGoals(userId, goals);
    }

    // Get recommended module titles for the authenticated user
    @GetMapping
    public List<String> getLearningPath() {
        Long userId = currentUserService.getCurrentUser().getId();
        return learningPathService.getLearningPath(userId);
    }

    // Get full learning path with user details for the authenticated user
    @GetMapping("/details")
    public Map<String, Object> getLearningPathDetails() {
        Long userId = currentUserService.getCurrentUser().getId();
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
