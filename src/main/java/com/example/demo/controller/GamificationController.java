package com.example.demo.controller;

import com.example.demo.model.UserProgress;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.GamificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    private final GamificationService gamificationService;
    private final CurrentUserService currentUserService;

    public GamificationController(GamificationService gamificationService,
                                   CurrentUserService currentUserService) {
        this.gamificationService = gamificationService;
        this.currentUserService = currentUserService;
    }

    // Mark a lesson complete and award points for the authenticated user
    @PostMapping("/complete-lesson")
    public UserProgress completeLesson(@RequestParam Long lessonId) {
        Long userId = currentUserService.getCurrentUser().getId();
        return gamificationService.completeLesson(userId, lessonId);
    }

    // Get top 10 leaderboard
    @GetMapping("/leaderboard")
    public List<GamificationService.LeaderboardEntry> getLeaderboard() {
        return gamificationService.getLeaderboard();
    }
}
