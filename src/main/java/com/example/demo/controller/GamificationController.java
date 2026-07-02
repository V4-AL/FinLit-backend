package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.UserProgress;
import com.example.demo.service.GamificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    // Mark a lesson complete and award points
    @PostMapping("/complete-lesson")
    public UserProgress completeLesson(
            @RequestParam Long userId,
            @RequestParam Long lessonId) {
        return gamificationService.completeLesson(userId, lessonId);
    }

    // Award points after a quiz
    @PostMapping("/award-quiz-points")
    public void awardQuizPoints(
            @RequestParam Long userId,
            @RequestParam int correctAnswers) {
        gamificationService.awardQuizPoints(userId, correctAnswers);
    }

    // Get top 10 leaderboard
    @GetMapping("/leaderboard")
    public List<User> getLeaderboard() {
        return gamificationService.getLeaderboard();
    }
}