package com.example.demo.repository;

import com.example.demo.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
}