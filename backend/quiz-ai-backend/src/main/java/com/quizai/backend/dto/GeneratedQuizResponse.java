package com.quizai.backend.dto;

import com.quizai.backend.model.Question;

import java.util.List;

public class GeneratedQuizResponse {

    private Long quizId;
    private List<Question> questions;

    public GeneratedQuizResponse() {
    }

    public GeneratedQuizResponse(Long quizId, List<Question> questions) {
        this.quizId = quizId;
        this.questions = questions;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
