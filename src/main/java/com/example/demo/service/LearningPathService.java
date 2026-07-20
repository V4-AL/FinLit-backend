package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.GoalType;
import com.example.demo.model.User;
import com.example.demo.repository.CourseModuleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LearningPathService {

    private final UserRepository userRepository;
    private final CourseModuleRepository moduleRepository;

    // Maps each goal to the module titles it should include
    private static final Map<String, List<String>> GOAL_TO_MODULES = new HashMap<>();

    static {
        GOAL_TO_MODULES.put(
            GoalType.AVOID_DEBT.name(),
            List.of("Investing and Managing Debt", "Introduction to Financial Literacy")
        );
        GOAL_TO_MODULES.put(
            GoalType.START_INVESTING.name(),
            List.of("Investing and Managing Debt", "Budgeting and Saving")
        );
        GOAL_TO_MODULES.put(
            GoalType.UNDERSTAND_MOBILE_MONEY.name(),
            List.of("Introduction to Financial Literacy", "Budgeting and Saving")
        );
        GOAL_TO_MODULES.put(
            GoalType.BUDGETING_BASICS.name(),
            List.of("Budgeting and Saving", "Introduction to Financial Literacy")
        );
        GOAL_TO_MODULES.put(
            GoalType.UNDERSTANDING_CREDIT.name(),
            List.of("Budgeting and Saving", "Investing and Managing Debt")
        );
    }

    public LearningPathService(UserRepository userRepository,
                                CourseModuleRepository moduleRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
    }

    public User setUserGoals(Long userId, List<String> goals) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate each goal against the enum
        List<String> validatedGoals = new ArrayList<>();
        for (String goal : goals) {
            try {
                GoalType.valueOf(goal.toUpperCase());
                validatedGoals.add(goal.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid goal: " + goal +
                    ". Valid goals are: " + Arrays.toString(GoalType.values()));
            }
        }

        user.setGoals(validatedGoals);
        return userRepository.save(user);
    }

    public List<String> getLearningPath(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getGoals() == null || user.getGoals().isEmpty()) {
            // Return all modules if no goals set
            return moduleRepository.findAll()
                    .stream()
                    .map(m -> m.getTitle())
                    .toList();
        }

        // Collect recommended modules in priority order, no duplicates
        LinkedHashSet<String> recommendedModules = new LinkedHashSet<>();
        for (String goal : user.getGoals()) {
            List<String> modulesForGoal = GOAL_TO_MODULES.getOrDefault(goal, List.of());
            recommendedModules.addAll(modulesForGoal);
        }

        return new ArrayList<>(recommendedModules);
    }

    public Map<String, Object> getLearningPathWithDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> moduleTitles = getLearningPath(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("username", user.getUsername());
        response.put("goals", user.getGoals());
        response.put("recommendedModules", moduleTitles);
        response.put("totalModules", moduleTitles.size());

        return response;
    }
}