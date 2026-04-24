package com.quizai.backend.dto.analytics;

public class SubtopicInsightResponse {

    private String topic;
    private String subtopic;
    private int accuracyPercentage;
    private String level;
    private int wrongAttempts;

    public SubtopicInsightResponse(String topic, String subtopic, int accuracyPercentage, String level, int wrongAttempts) {
        this.topic = topic;
        this.subtopic = subtopic;
        this.accuracyPercentage = accuracyPercentage;
        this.level = level;
        this.wrongAttempts = wrongAttempts;
    }

    public String getTopic() {
        return topic;
    }

    public String getSubtopic() {
        return subtopic;
    }

    public int getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public String getLevel() {
        return level;
    }

    public int getWrongAttempts() {
        return wrongAttempts;
    }
}
