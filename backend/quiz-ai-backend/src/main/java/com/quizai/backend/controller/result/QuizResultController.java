package com.quizai.backend.controller.result;

import com.quizai.backend.dto.result.LeaderboardEntryResponse;
import com.quizai.backend.dto.result.QuizHistoryResponse;
import com.quizai.backend.dto.result.SaveQuizResultRequest;
import com.quizai.backend.dto.result.SaveQuizResultReportRequest;
import com.quizai.backend.service.QuizResultService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    @PostMapping("/api/quiz-results")
    public QuizHistoryResponse saveQuizResult(@RequestBody SaveQuizResultRequest request) {
        try {
            return quizResultService.saveResult(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/api/leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard() {
        return quizResultService.getLeaderboard();
    }

    @GetMapping("/api/quiz-history/{userId}")
    public List<QuizHistoryResponse> getQuizHistory(@PathVariable Long userId) {
        try {
            return quizResultService.getHistoryByUserId(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PostMapping("/api/quiz-results/{id}/report")
    public void saveQuizResultReport(@PathVariable Long id, @RequestBody SaveQuizResultReportRequest request) {
        try {
            quizResultService.saveReport(id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }

    @GetMapping("/api/quiz/result/{id}/pdf")
    public ResponseEntity<byte[]> downloadQuizResultPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = quizResultService.generateReportPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz-result-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
}
