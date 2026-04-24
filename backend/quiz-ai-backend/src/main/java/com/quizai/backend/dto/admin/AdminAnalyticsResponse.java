package com.quizai.backend.dto.admin;

public class AdminAnalyticsResponse {

    private long totalUsers;
    private long totalQuizzes;
    private long totalUploadedFiles;
    private int averageQuizScore;

    public AdminAnalyticsResponse(long totalUsers, long totalQuizzes, long totalUploadedFiles, int averageQuizScore) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalUploadedFiles = totalUploadedFiles;
        this.averageQuizScore = averageQuizScore;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public long getTotalUploadedFiles() {
        return totalUploadedFiles;
    }

    public int getAverageQuizScore() {
        return averageQuizScore;
    }
}
