package com.quizai.backend.dto.admin;

public class AdminQuestionReviewResponse {

    private Long id;
    private Long quizId;
    private String question;
    private String correctAnswer;

    public AdminQuestionReviewResponse(Long id, Long quizId, String question, String correctAnswer) {
        this.id = id;
        this.quizId = quizId;
        this.question = question;
        this.correctAnswer = correctAnswer;
    }

    public Long getId() {
        return id;
    }

    public Long getQuizId() {
        return quizId;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
