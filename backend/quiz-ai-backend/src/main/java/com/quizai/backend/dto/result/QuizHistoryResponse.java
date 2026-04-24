package com.quizai.backend.dto.result;

import java.time.LocalDateTime;

public class QuizHistoryResponse {

    private Long id;
    private Integer score;
    private Integer totalQuestions;
    private LocalDateTime date;

    public QuizHistoryResponse() {
    }

    public QuizHistoryResponse(Long id, Integer score, Integer totalQuestions, LocalDateTime date) {
        this.id = id;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
