package com.quizai.backend.controller;

import com.quizai.backend.dto.GenerateQuizRequest;
import com.quizai.backend.dto.GeneratedQuizResponse;
import com.quizai.backend.dto.AnswerExplanationRequest;
import com.quizai.backend.dto.AnswerExplanationResponse;
import com.quizai.backend.model.Quiz;
import com.quizai.backend.service.AIQuizService;
import com.quizai.backend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizController {

    private final QuizService quizService;
    private final AIQuizService aiQuizService;

    public QuizController(QuizService quizService, AIQuizService aiQuizService) {
        this.quizService = quizService;
        this.aiQuizService = aiQuizService;
    }

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    @GetMapping
    public List<Quiz> getAllQuiz() {
        return quizService.getAllQuiz();
    }

    @PostMapping("/generate")
    public GeneratedQuizResponse generateQuiz(@RequestBody GenerateQuizRequest request) {
        try {
            return aiQuizService.generateQuiz(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/explanation")
    public AnswerExplanationResponse generateExplanations(@RequestBody AnswerExplanationRequest request) {
        try {
            return aiQuizService.generateAnswerExplanations(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}