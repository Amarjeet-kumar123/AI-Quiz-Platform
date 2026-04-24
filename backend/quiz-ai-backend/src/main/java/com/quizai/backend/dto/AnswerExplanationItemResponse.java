package com.quizai.backend.dto;

public class AnswerExplanationItemResponse {

    private Integer questionIndex;
    private String explanation;
    private String wrongOptionExplanation;

    public AnswerExplanationItemResponse() {
    }

    public AnswerExplanationItemResponse(Integer questionIndex, String explanation, String wrongOptionExplanation) {
        this.questionIndex = questionIndex;
        this.explanation = explanation;
        this.wrongOptionExplanation = wrongOptionExplanation;
    }

    public Integer getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(Integer questionIndex) {
        this.questionIndex = questionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getWrongOptionExplanation() {
        return wrongOptionExplanation;
    }

    public void setWrongOptionExplanation(String wrongOptionExplanation) {
        this.wrongOptionExplanation = wrongOptionExplanation;
    }
}
