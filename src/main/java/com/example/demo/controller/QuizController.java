package com.example.demo.controller;

import com.example.demo.model.Question;
import com.example.demo.model.Quiz;
import com.example.demo.model.QuizResult;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.QuizService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUserService currentUserService;

    public QuizController(QuizService quizService, CurrentUserService currentUserService) {
        this.quizService = quizService;
        this.currentUserService = currentUserService;
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
            @RequestBody Map<String, String> answers) {
        Long userId = currentUserService.getCurrentUser().getId();
        return quizService.submitQuiz(quizId, userId, answers);
    }
}
