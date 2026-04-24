package com.quizai.backend.controller.admin;

import com.quizai.backend.dto.admin.*;
import com.quizai.backend.dto.result.LeaderboardEntryResponse;
import com.quizai.backend.model.Document;
import com.quizai.backend.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        try {
            return adminService.login(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        }
    }

    @GetMapping("/analytics")
    public AdminAnalyticsResponse getAnalytics(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getAnalytics();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getUsers();
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@RequestHeader("X-Admin-Token") String adminToken, @PathVariable Long id) {
        validate(adminToken);
        try {
            adminService.deleteUser(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/documents")
    public List<Document> getDocuments(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getDocuments();
    }

    @DeleteMapping("/documents/{id}")
    public void deleteDocument(@RequestHeader("X-Admin-Token") String adminToken, @PathVariable Long id) {
        validate(adminToken);
        try {
            adminService.deleteDocument(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/quizzes")
    public List<AdminQuizResponse> getQuizzes(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getQuizzes();
    }

    @DeleteMapping("/quizzes/{id}")
    public void deleteQuiz(@RequestHeader("X-Admin-Token") String adminToken, @PathVariable Long id) {
        validate(adminToken);
        try {
            adminService.deleteQuiz(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getLeaderboard();
    }

    @GetMapping("/questions/recent")
    public List<AdminQuestionReviewResponse> getRecentQuestions(@RequestHeader("X-Admin-Token") String adminToken) {
        validate(adminToken);
        return adminService.getRecentQuestions();
    }

    private void validate(String adminToken) {
        try {
            adminService.validateAdminToken(adminToken);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        }
    }
}
