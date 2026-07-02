package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.UserProgress;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamificationService {

    private static final int POINTS_PER_LESSON = 10;
    private static final int POINTS_PER_QUIZ_CORRECT = 5;

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    public GamificationService(UserRepository userRepository,
                                UserProgressRepository userProgressRepository) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public UserProgress completeLesson(Long userId, Long lessonId) {
        // Don't award points if already completed
        if (userProgressRepository.existsByUserIdAndLessonId(userId, lessonId)) {
            return userProgressRepository.findByUserId(userId)
                    .stream()
                    .filter(p -> p.getLessonId().equals(lessonId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Progress not found"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Award points
        user.setTotalPoints(user.getTotalPoints() + POINTS_PER_LESSON);

        // Update streak
        user.setCurrentStreak(user.getCurrentStreak() + 1);
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        // Check and award badges
        awardBadges(user);
        userRepository.save(user);

        // Save progress
        UserProgress progress = new UserProgress();
        progress.setUser(user);
        progress.setLessonId(lessonId);
        progress.setCompleted(true);
        progress.setCompletionDate(LocalDateTime.now());
        progress.setPointsEarned(POINTS_PER_LESSON);

        return userProgressRepository.save(progress);
    }

    public void awardQuizPoints(Long userId, int correctAnswers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int pointsToAdd = correctAnswers * POINTS_PER_QUIZ_CORRECT;
        user.setTotalPoints(user.getTotalPoints() + pointsToAdd);

        awardBadges(user);
        userRepository.save(user);
    }

    private void awardBadges(User user) {
        List<String> badges = user.getBadges();
        if (badges == null) {
            badges = new ArrayList<>();
        }

        // First lesson badge
        if (user.getTotalPoints() >= POINTS_PER_LESSON && !badges.contains("FIRST_LESSON")) {
            badges.add("FIRST_LESSON");
        }
        // 7-day streak badge
        if (user.getCurrentStreak() >= 7 && !badges.contains("WEEK_STREAK")) {
            badges.add("WEEK_STREAK");
        }
        // 50 points badge
        if (user.getTotalPoints() >= 50 && !badges.contains("RISING_STAR")) {
            badges.add("RISING_STAR");
        }
        // 100 points badge
        if (user.getTotalPoints() >= 100 && !badges.contains("SCHOLAR")) {
            badges.add("SCHOLAR");
        }
        // 30-day streak badge
        if (user.getCurrentStreak() >= 30 && !badges.contains("DEDICATED_LEARNER")) {
            badges.add("DEDICATED_LEARNER");
        }

        user.setBadges(badges);
    }

    public List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByTotalPointsDesc();
    }
}