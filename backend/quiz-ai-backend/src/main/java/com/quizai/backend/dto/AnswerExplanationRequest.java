package com.quizai.backend.dto;

import java.util.List;

public class AnswerExplanationRequest {

    private String topic;
    private String difficulty;
    private List<AnswerExplanationItemRequest> items;

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

    public List<AnswerExplanationItemRequest> getItems() {
        return items;
    }

    public void setItems(List<AnswerExplanationItemRequest> items) {
        this.items = items;
    }
}
