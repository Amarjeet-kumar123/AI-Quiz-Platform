package com.quizai.backend.controller.analytics;

import com.quizai.backend.dto.analytics.WeaknessAnalyticsResponse;
import com.quizai.backend.service.WeaknessAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    private final WeaknessAnalyticsService weaknessAnalyticsService;

    public AnalyticsController(WeaknessAnalyticsService weaknessAnalyticsService) {
        this.weaknessAnalyticsService = weaknessAnalyticsService;
    }

    @GetMapping("/weakness/{userId}")
    public WeaknessAnalyticsResponse getWeaknessAnalytics(@PathVariable Long userId) {
        try {
            return weaknessAnalyticsService.analyze(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
