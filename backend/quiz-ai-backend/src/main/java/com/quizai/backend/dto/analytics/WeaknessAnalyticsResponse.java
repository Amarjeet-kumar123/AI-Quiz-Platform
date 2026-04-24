package com.quizai.backend.dto.analytics;

import java.util.List;

public class WeaknessAnalyticsResponse {

    private List<TopicPerformanceResponse> topicPerformance;
    private List<SubtopicInsightResponse> subtopicInsights;
    private List<String> weakAreas;
    private List<String> strongAreas;
    private List<String> recommendedNextTopics;
    private List<String> personalizedSuggestions;
    private String suggestedNextDifficulty;

    public WeaknessAnalyticsResponse(
            List<TopicPerformanceResponse> topicPerformance,
            List<SubtopicInsightResponse> subtopicInsights,
            List<String> weakAreas,
            List<String> strongAreas,
            List<String> recommendedNextTopics,
            List<String> personalizedSuggestions,
            String suggestedNextDifficulty
    ) {
        this.topicPerformance = topicPerformance;
        this.subtopicInsights = subtopicInsights;
        this.weakAreas = weakAreas;
        this.strongAreas = strongAreas;
        this.recommendedNextTopics = recommendedNextTopics;
        this.personalizedSuggestions = personalizedSuggestions;
        this.suggestedNextDifficulty = suggestedNextDifficulty;
    }

    public List<TopicPerformanceResponse> getTopicPerformance() {
        return topicPerformance;
    }

    public List<SubtopicInsightResponse> getSubtopicInsights() {
        return subtopicInsights;
    }

    public List<String> getWeakAreas() {
        return weakAreas;
    }

    public List<String> getStrongAreas() {
        return strongAreas;
    }

    public List<String> getRecommendedNextTopics() {
        return recommendedNextTopics;
    }

    public List<String> getPersonalizedSuggestions() {
        return personalizedSuggestions;
    }

    public String getSuggestedNextDifficulty() {
        return suggestedNextDifficulty;
    }
}
