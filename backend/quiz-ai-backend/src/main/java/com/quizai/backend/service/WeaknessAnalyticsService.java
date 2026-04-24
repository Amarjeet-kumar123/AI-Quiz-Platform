package com.quizai.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizai.backend.dto.analytics.SubtopicInsightResponse;
import com.quizai.backend.dto.analytics.TopicPerformanceResponse;
import com.quizai.backend.dto.analytics.WeaknessAnalyticsResponse;
import com.quizai.backend.model.QuizResult;
import com.quizai.backend.model.QuizResultReport;
import com.quizai.backend.repository.QuizResultReportRepository;
import com.quizai.backend.repository.QuizResultRepository;
import com.quizai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WeaknessAnalyticsService {

    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizResultReportRepository quizResultReportRepository;
    private final ObjectMapper objectMapper;

    public WeaknessAnalyticsService(
            UserRepository userRepository,
            QuizResultRepository quizResultRepository,
            QuizResultReportRepository quizResultReportRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.quizResultRepository = quizResultRepository;
        this.quizResultReportRepository = quizResultReportRepository;
        this.objectMapper = objectMapper;
    }

    public WeaknessAnalyticsResponse analyze(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("User does not exist.");
        }

        List<QuizResult> userResults = quizResultRepository.findByUserIdOrderByDateDesc(userId);
        if (userResults.isEmpty()) {
            return new WeaknessAnalyticsResponse(
                    List.of(),
                    List.of(),
                    List.of("Complete at least one quiz to identify weak areas."),
                    List.of("Start with foundation topics to build consistency."),
                    List.of("Java Basics"),
                    List.of("Attempt 2-3 easy quizzes first to generate intelligent insights."),
                    "Continue practicing Easy difficulty first."
            );
        }

        List<Long> resultIds = userResults.stream().map(QuizResult::getId).toList();
        List<QuizResultReport> reports = quizResultReportRepository.findByQuizResultIdIn(resultIds);
        if (reports.isEmpty()) {
            return new WeaknessAnalyticsResponse(
                    List.of(),
                    List.of(),
                    List.of("Detailed analytics will appear after new quiz attempts."),
                    List.of("Quiz completion consistency is improving."),
                    List.of("Continue current topic"),
                    List.of("Submit one new quiz attempt to unlock weakness analysis."),
                    "Continue practicing Easy difficulty first."
            );
        }

        Map<String, List<Integer>> topicScores = new HashMap<>();
        Map<String, SubtopicStats> subtopicStats = new HashMap<>();
        Map<String, List<Integer>> difficultyScores = new HashMap<>();

        for (QuizResultReport report : reports) {
            String topic = report.getTopic() == null ? "General" : report.getTopic().trim();
            String difficulty = report.getDifficulty() == null ? "Easy" : report.getDifficulty().trim();

            topicScores.computeIfAbsent(topic, key -> new ArrayList<>()).add(report.getAccuracyPercentage());
            difficultyScores.computeIfAbsent(difficulty, key -> new ArrayList<>()).add(report.getAccuracyPercentage());

            List<Map<String, Object>> answerRows = parseAnswerRows(report.getAnswerReviewJson());
            for (Map<String, Object> row : answerRows) {
                String question = String.valueOf(row.getOrDefault("question", ""));
                String status = String.valueOf(row.getOrDefault("status", "Wrong"));
                String subtopic = classifySubtopic(question);
                String subtopicKey = topic + "::" + subtopic;
                SubtopicStats stats = subtopicStats.computeIfAbsent(subtopicKey, key -> new SubtopicStats(topic, subtopic));
                stats.total++;
                if ("Correct".equalsIgnoreCase(status)) {
                    stats.correct++;
                } else {
                    stats.wrong++;
                }
            }
        }

        List<TopicPerformanceResponse> topicPerformance = topicScores.entrySet().stream()
                .map(entry -> new TopicPerformanceResponse(entry.getKey(), average(entry.getValue())))
                .sorted(Comparator.comparing(TopicPerformanceResponse::getAccuracyPercentage).reversed())
                .toList();

        List<SubtopicInsightResponse> subtopicInsights = subtopicStats.values().stream()
                .map(stats -> {
                    int acc = stats.total == 0 ? 0 : Math.round((stats.correct * 100f) / stats.total);
                    return new SubtopicInsightResponse(
                            stats.topic,
                            stats.subtopic,
                            acc,
                            levelFor(acc),
                            stats.wrong
                    );
                })
                .sorted(Comparator.comparing(SubtopicInsightResponse::getAccuracyPercentage))
                .toList();

        List<String> weakAreas = subtopicInsights.stream()
                .filter(item -> "Weak".equals(item.getLevel()))
                .map(item -> item.getTopic() + " - " + item.getSubtopic() + " (" + item.getAccuracyPercentage() + "%)")
                .limit(5)
                .toList();

        List<String> strongAreas = subtopicInsights.stream()
                .filter(item -> "Strong".equals(item.getLevel()))
                .map(item -> item.getTopic() + " - " + item.getSubtopic() + " (" + item.getAccuracyPercentage() + "%)")
                .limit(5)
                .toList();

        List<String> recommendedTopics = topicPerformance.stream()
                .sorted(Comparator.comparing(TopicPerformanceResponse::getAccuracyPercentage))
                .limit(3)
                .map(item -> "Practice " + item.getTopic() + " next")
                .toList();

        List<String> suggestions = subtopicInsights.stream()
                .filter(item -> "Weak".equals(item.getLevel()) || "Medium".equals(item.getLevel()))
                .limit(4)
                .map(item -> "Revise " + item.getTopic() + " " + item.getSubtopic() + " and solve targeted MCQs.")
                .toList();

        String suggestedDifficulty = suggestDifficulty(difficultyScores, reports);

        return new WeaknessAnalyticsResponse(
                topicPerformance,
                subtopicInsights,
                weakAreas.isEmpty() ? List.of("No major weak areas detected yet.") : weakAreas,
                strongAreas.isEmpty() ? List.of("Strong areas will appear after more attempts.") : strongAreas,
                recommendedTopics.isEmpty() ? List.of("Continue current learning track") : recommendedTopics,
                suggestions.isEmpty() ? List.of("Maintain momentum with mixed-topic revision quizzes.") : suggestions,
                suggestedDifficulty
        );
    }

    private List<Map<String, Object>> parseAnswerRows(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String classifySubtopic(String question) {
        String q = question == null ? "" : question.toLowerCase();
        if (q.contains("exception")) return "Exception Handling";
        if (q.contains("thread") || q.contains("concurrency")) return "Multithreading";
        if (q.contains("collection") || q.contains("list") || q.contains("map")) return "Collections";
        if (q.contains("polymorphism")) return "Polymorphism";
        if (q.contains("encapsulation")) return "Encapsulation";
        if (q.contains("inheritance")) return "Inheritance";
        if (q.contains("abstraction") || q.contains("interface")) return "Abstraction & Interfaces";
        if (q.contains("join") || q.contains("sql")) return "SQL Queries";
        if (q.contains("normalization")) return "Normalization";
        if (q.contains("transaction") || q.contains("acid")) return "Transactions";
        if (q.contains("process") || q.contains("thread")) return "Process Management";
        if (q.contains("memory") || q.contains("paging")) return "Memory Management";
        if (q.contains("deadlock")) return "Deadlock";
        if (q.contains("object") || q.contains("class") || q.contains("oop")) return "OOP";
        return "Core Concepts";
    }

    private int average(List<Integer> values) {
        if (values == null || values.isEmpty()) return 0;
        return Math.round((float) values.stream().mapToInt(Integer::intValue).sum() / values.size());
    }

    private String levelFor(int accuracy) {
        if (accuracy >= 75) return "Strong";
        if (accuracy >= 50) return "Medium";
        return "Weak";
    }

    private String suggestDifficulty(Map<String, List<Integer>> difficultyScores, List<QuizResultReport> reports) {
        QuizResultReport latest = reports.stream()
                .max(Comparator.comparing(QuizResultReport::getTakenAt))
                .orElse(null);
        if (latest == null) {
            return "Continue practicing Easy difficulty first.";
        }

        String latestDifficulty = latest.getDifficulty() == null ? "Easy" : latest.getDifficulty();
        int latestDifficultyAvg = average(difficultyScores.getOrDefault(latestDifficulty, List.of()));

        if (latestDifficultyAvg >= 78) {
            if ("Easy".equalsIgnoreCase(latestDifficulty)) {
                return "Try Medium difficulty next for deeper concept application.";
            }
            if ("Medium".equalsIgnoreCase(latestDifficulty)) {
                return "Try Hard difficulty next to challenge your problem-solving depth.";
            }
            return "Continue Hard difficulty and focus on weak subtopics.";
        }

        if (latestDifficultyAvg < 55) {
            if ("Hard".equalsIgnoreCase(latestDifficulty)) {
                return "Shift to Medium difficulty and strengthen fundamentals first.";
            }
            if ("Medium".equalsIgnoreCase(latestDifficulty)) {
                return "Continue practicing Easy difficulty first.";
            }
            return "Stay on Easy difficulty and improve accuracy before moving up.";
        }

        return "Continue with current difficulty and target weak subtopics.";
    }

    private static class SubtopicStats {
        private final String topic;
        private final String subtopic;
        private int total;
        private int correct;
        private int wrong;

        private SubtopicStats(String topic, String subtopic) {
            this.topic = topic;
            this.subtopic = subtopic;
        }
    }
}
