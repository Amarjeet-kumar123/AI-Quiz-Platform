package com.quizai.backend.dto.result;

public class LeaderboardEntryResponse {

    private Long userId;
    private String username;
    private Integer bestScore;
    private Integer attempts;

    public LeaderboardEntryResponse() {
    }

    public LeaderboardEntryResponse(Long userId, String username, Integer bestScore, Integer attempts) {
        this.userId = userId;
        this.username = username;
        this.bestScore = bestScore;
        this.attempts = attempts;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public Integer getAttempts() {
        return attempts;
    }
}
