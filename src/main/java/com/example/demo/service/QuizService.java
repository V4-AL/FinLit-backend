package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Question;
import com.example.demo.model.Quiz;
import com.example.demo.model.QuizResult;
import com.example.demo.model.User;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       QuizResultRepository quizResultRepository,
                       UserRepository userRepository,
                       GamificationService gamificationService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    public List<Quiz> getQuizzesByCourseModule(Long courseModuleId) {
        return quizRepository.findByCourseModuleId(courseModuleId);
    }

    public List<Question> getQuestionsByQuiz(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    public QuizResult submitQuiz(Long quizId, Long userId, Map<String, String> answers) {
        List<Question> questions = questionRepository.findByQuizId(quizId);

        int score = 0;
        for (Question q : questions) {
            String submitted = answers.get(String.valueOf(q.getId()));
            if (submitted != null && submitted.equalsIgnoreCase(q.getCorrectAnswer())) {
                score++;
            }
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setUser(user);
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        result.setCompletedAt(LocalDateTime.now());

        QuizResult saved = quizResultRepository.save(result);

        // Points are awarded from the server-computed score, never a client-supplied count.
        gamificationService.awardQuizPoints(userId, score);

        return saved;
    }
}
