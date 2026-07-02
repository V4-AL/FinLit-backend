package com.example.demo.controller;

import com.example.demo.dto.Question;
import com.example.demo.dto.Quiz;
import com.example.demo.dto.QuizResult;
import com.example.demo.service.QuizService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/course-module/{courseModuleId}")
    public List<Quiz> getQuizzesByCourseModule(@PathVariable Long courseModuleId) {
        return quizService.getQuizzesByCourseModule(courseModuleId);
    }

    @GetMapping("/{quizId}/questions")
    public List<Question> getQuestions(@PathVariable Long quizId) {
        return quizService.getQuestionsByQuiz(quizId);
    }

    @PostMapping("/{quizId}/submit")
    public QuizResult submitQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId,
            @RequestBody Map<String, String> answers) {
        return quizService.submitQuiz(quizId, userId, answers);
    }
}
