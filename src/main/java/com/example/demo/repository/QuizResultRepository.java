package com.example.demo.repository;

import com.example.demo.dto.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUser_Id(Long userId);
    List<QuizResult> findByQuiz_Id(Long quizId);
}