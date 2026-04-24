package com.quizai.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_result_report")
public class QuizResultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long quizResultId;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private Integer numberOfQuestions;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer accuracyPercentage;

    @Column(nullable = false)
    private LocalDateTime takenAt;

    @Lob
    @Column(nullable = false)
    private String answerReviewJson;

    @Lob
    @Column(nullable = false)
    private String performanceSummaryJson;

    @Lob
    @Column(nullable = false)
    private String improvementSuggestionsJson;

    public Long getId() {
        return id;
    }

    public Long getQuizResultId() {
        return quizResultId;
    }

    public void setQuizResultId(Long quizResultId) {
        this.quizResultId = quizResultId;
    }

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

    public String getAnswerReviewJson() {
        return answerReviewJson;
    }

    public void setAnswerReviewJson(String answerReviewJson) {
        this.answerReviewJson = answerReviewJson;
    }

    public String getPerformanceSummaryJson() {
        return performanceSummaryJson;
    }

    public void setPerformanceSummaryJson(String performanceSummaryJson) {
        this.performanceSummaryJson = performanceSummaryJson;
    }

    public String getImprovementSuggestionsJson() {
        return improvementSuggestionsJson;
    }

    public void setImprovementSuggestionsJson(String improvementSuggestionsJson) {
        this.improvementSuggestionsJson = improvementSuggestionsJson;
    }
}
