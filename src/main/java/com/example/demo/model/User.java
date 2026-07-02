package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;

    // Gamification
    private int totalPoints = 0;
    private int spentPoints = 0;
    private int currentStreak = 0;
    private int longestStreak = 0;

    @ElementCollection
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "badge")
    private List<String> badges;

    // Personalization
    @ElementCollection
    @CollectionTable(name = "user_goals", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "goal")
    private List<String> goals;

    public int getAvailablePoints() {
        return totalPoints - spentPoints;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getSpentPoints() { return spentPoints; }
    public void setSpentPoints(int spentPoints) { this.spentPoints = spentPoints; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
    public List<String> getGoals() { return goals; }
    public void setGoals(List<String> goals) { this.goals = goals; }
}