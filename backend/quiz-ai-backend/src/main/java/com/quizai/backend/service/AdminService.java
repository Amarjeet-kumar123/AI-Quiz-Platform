package com.quizai.backend.service;

import com.quizai.backend.dto.admin.*;
import com.quizai.backend.dto.result.LeaderboardEntryResponse;
import com.quizai.backend.model.Document;
import com.quizai.backend.model.Question;
import com.quizai.backend.model.Quiz;
import com.quizai.backend.model.QuizResult;
import com.quizai.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminService {

    private static final long ADMIN_TOKEN_TTL_MS = 8L * 60L * 60L * 1000L;

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizResultService quizResultService;

    private final String adminUsername;
    private final String adminPassword;

    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();

    public AdminService(
            UserRepository userRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            QuizResultRepository quizResultRepository,
            QuizResultService quizResultService,
            @Value("${admin.username:admin}") String adminUsername,
            @Value("${admin.password:admin123}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.quizResultRepository = quizResultRepository;
        this.quizResultService = quizResultService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        if (!adminUsername.equals(request.getUsername().trim()) || !adminPassword.equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }

        String tokenPayload = adminUsername + ":" + System.currentTimeMillis();
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenPayload.getBytes(StandardCharsets.UTF_8));
        activeTokens.put(token, System.currentTimeMillis() + ADMIN_TOKEN_TTL_MS);
        return new AdminLoginResponse(token, adminUsername);
    }

    public void validateAdminToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Admin token is required.");
        }
        Long expiresAt = activeTokens.get(token);
        if (expiresAt == null) {
            throw new IllegalArgumentException("Invalid admin token.");
        }
        if (expiresAt < System.currentTimeMillis()) {
            activeTokens.remove(token);
            throw new IllegalArgumentException("Admin session expired. Please login again.");
        }
    }

    public AdminAnalyticsResponse getAnalytics() {
        List<QuizResult> results = quizResultRepository.findAll();
        int averageScore = 0;
        if (!results.isEmpty()) {
            double avg = results.stream()
                    .mapToDouble(result -> (double) result.getScore() * 100.0 / result.getTotalQuestions())
                    .average()
                    .orElse(0.0);
            averageScore = (int) Math.round(avg);
        }
        return new AdminAnalyticsResponse(
                userRepository.count(),
                quizRepository.count(),
                documentRepository.count(),
                averageScore
        );
    }

    public List<AdminUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }

    public void deleteUser(Long userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found.");
        }
        quizResultRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(Long documentId) {
        if (documentId == null || !documentRepository.existsById(documentId)) {
            throw new IllegalArgumentException("Document not found.");
        }
        documentChunkRepository.deleteByDocumentId(documentId);
        documentRepository.deleteById(documentId);
    }

    public List<AdminQuizResponse> getQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> new AdminQuizResponse(
                        quiz.getId(),
                        quiz.getTopic(),
                        quiz.getDifficulty(),
                        quiz.getCreatedAt(),
                        questionRepository.findByQuizId(quiz.getId()).size()
                ))
                .toList();
    }

    public void deleteQuiz(Long quizId) {
        if (quizId == null || !quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Quiz not found.");
        }
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }

    public List<LeaderboardEntryResponse> getLeaderboard() {
        return quizResultService.getLeaderboard();
    }

    public List<AdminQuestionReviewResponse> getRecentQuestions() {
        return questionRepository.findTop30ByOrderByIdDesc().stream()
                .map(question -> new AdminQuestionReviewResponse(
                        question.getId(),
                        question.getQuizId(),
                        question.getQuestion(),
                        question.getCorrectAnswer()
                ))
                .toList();
    }
}
