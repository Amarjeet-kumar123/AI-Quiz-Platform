package com.quizai.backend.dto.admin;

import java.time.LocalDateTime;

public class AdminQuizResponse {

    private Long id;
    private String topic;
    private String difficulty;
    private LocalDateTime createdAt;
    private Integer questionCount;

    public AdminQuizResponse(Long id, String topic, String difficulty, LocalDateTime createdAt, Integer questionCount) {
        this.id = id;
        this.topic = topic;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.questionCount = questionCount;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }
}
