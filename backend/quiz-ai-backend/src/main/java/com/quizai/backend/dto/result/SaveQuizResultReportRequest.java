package com.quizai.backend.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public class SaveQuizResultReportRequest {

    private String topic;
    private String difficulty;
    private Integer numberOfQuestions;
    private Integer score;
    private Integer accuracyPercentage;
    private LocalDateTime takenAt;
    private List<AnswerReviewItemRequest> answerReview;
    private List<String> performanceSummary;
    private List<String> improvementSuggestions;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(Integer accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public List<AnswerReviewItemRequest> getAnswerReview() {
        return answerReview;
    }

    public void setAnswerReview(List<AnswerReviewItemRequest> answerReview) {
        this.answerReview = answerReview;
    }

    public List<String> getPerformanceSummary() {
        return performanceSummary;
    }

    public void setPerformanceSummary(List<String> performanceSummary) {
        this.performanceSummary = performanceSummary;
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }
}
