package com.quizai.backend.dto.analytics;

public class TopicPerformanceResponse {

    private String topic;
    private int accuracyPercentage;

    public TopicPerformanceResponse(String topic, int accuracyPercentage) {
        this.topic = topic;
        this.accuracyPercentage = accuracyPercentage;
    }

    public String getTopic() {
        return topic;
    }

    public int getAccuracyPercentage() {
        return accuracyPercentage;
    }
}
