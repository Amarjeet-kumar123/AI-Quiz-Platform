package com.quizai.backend.dto;

import java.util.List;

public class AnswerExplanationResponse {

    private List<AnswerExplanationItemResponse> items;

    public AnswerExplanationResponse() {
    }

    public AnswerExplanationResponse(List<AnswerExplanationItemResponse> items) {
        this.items = items;
    }

    public List<AnswerExplanationItemResponse> getItems() {
        return items;
    }

    public void setItems(List<AnswerExplanationItemResponse> items) {
        this.items = items;
    }
}
