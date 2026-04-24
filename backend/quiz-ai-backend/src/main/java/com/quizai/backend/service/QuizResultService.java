package com.quizai.backend.service;

import com.quizai.backend.dto.result.LeaderboardEntryResponse;
import com.quizai.backend.dto.result.QuizHistoryResponse;
import com.quizai.backend.dto.result.SaveQuizResultRequest;
import com.quizai.backend.dto.result.SaveQuizResultReportRequest;
import com.quizai.backend.model.QuizResult;
import com.quizai.backend.model.QuizResultReport;
import com.quizai.backend.model.User;
import com.quizai.backend.repository.QuizResultRepository;
import com.quizai.backend.repository.QuizResultReportRepository;
import com.quizai.backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;
    private final QuizResultReportRepository quizResultReportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuizResultService(
            QuizResultRepository quizResultRepository,
            QuizResultReportRepository quizResultReportRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.quizResultRepository = quizResultRepository;
        this.quizResultReportRepository = quizResultReportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public QuizHistoryResponse saveResult(SaveQuizResultRequest request) {
        validateSaveRequest(request);

        if (userRepository.findById(request.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("User does not exist.");
        }

        QuizResult result = new QuizResult();
        result.setUserId(request.getUserId());
        result.setScore(request.getScore());
        result.setTotalQuestions(request.getTotalQuestions());

        QuizResult saved = quizResultRepository.save(result);
        return new QuizHistoryResponse(saved.getId(), saved.getScore(), saved.getTotalQuestions(), saved.getDate());
    }

    public List<QuizHistoryResponse> getHistoryByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }

        return quizResultRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(result -> new QuizHistoryResponse(
                        result.getId(),
                        result.getScore(),
                        result.getTotalQuestions(),
                        result.getDate()
                ))
                .toList();
    }

    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<QuizResult> allResults = quizResultRepository.findAll();
        if (allResults.isEmpty()) {
            return List.of();
        }

        Map<Long, List<QuizResult>> byUser = allResults.stream()
                .collect(Collectors.groupingBy(QuizResult::getUserId));

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        for (Map.Entry<Long, List<QuizResult>> entry : byUser.entrySet()) {
            Long userId = entry.getKey();
            List<QuizResult> userResults = entry.getValue();
            int bestScore = userResults.stream().mapToInt(QuizResult::getScore).max().orElse(0);
            int attempts = userResults.size();
            String username = userRepository.findById(userId).map(User::getUsername).orElse("Unknown");
            leaderboard.add(new LeaderboardEntryResponse(userId, username, bestScore, attempts));
        }

        leaderboard.sort(Comparator
                .comparing(LeaderboardEntryResponse::getBestScore, Comparator.reverseOrder())
                .thenComparing(LeaderboardEntryResponse::getAttempts, Comparator.reverseOrder()));
        return leaderboard;
    }

    public void saveReport(Long quizResultId, SaveQuizResultReportRequest request) {
        if (quizResultId == null) {
            throw new IllegalArgumentException("quizResultId is required.");
        }
        if (quizResultRepository.findById(quizResultId).isEmpty()) {
            throw new IllegalArgumentException("Quiz result not found.");
        }
        validateReportRequest(request);

        QuizResultReport report = quizResultReportRepository.findByQuizResultId(quizResultId)
                .orElseGet(QuizResultReport::new);
        report.setQuizResultId(quizResultId);
        report.setTopic(request.getTopic().trim());
        report.setDifficulty(request.getDifficulty().trim());
        report.setNumberOfQuestions(request.getNumberOfQuestions());
        report.setScore(request.getScore());
        report.setAccuracyPercentage(request.getAccuracyPercentage());
        report.setTakenAt(request.getTakenAt() == null ? LocalDateTime.now() : request.getTakenAt());

        try {
            report.setAnswerReviewJson(objectMapper.writeValueAsString(request.getAnswerReview()));
            report.setPerformanceSummaryJson(objectMapper.writeValueAsString(request.getPerformanceSummary()));
            report.setImprovementSuggestionsJson(objectMapper.writeValueAsString(request.getImprovementSuggestions()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to prepare quiz report data.", ex);
        }

        quizResultReportRepository.save(report);
    }

    public byte[] generateReportPdf(Long quizResultId) {
        QuizResultReport report = quizResultReportRepository.findByQuizResultId(quizResultId)
                .orElseThrow(() -> new IllegalArgumentException("Detailed report not found for this quiz result."));

        List<Map<String, Object>> answerRows;
        List<String> performanceSummary;
        List<String> improvementSuggestions;
        try {
            answerRows = objectMapper.readValue(report.getAnswerReviewJson(), new TypeReference<>() {});
            performanceSummary = objectMapper.readValue(report.getPerformanceSummaryJson(), new TypeReference<>() {});
            improvementSuggestions = objectMapper.readValue(report.getImprovementSuggestionsJson(), new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse quiz report data.", ex);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("AI Quiz Platform - Quiz Performance Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(14);
            document.add(title);

            document.add(new Paragraph("1) Student Quiz Summary", sectionFont));
            document.add(new Paragraph("Quiz Topic: " + report.getTopic(), bodyFont));
            document.add(new Paragraph("Difficulty Level: " + report.getDifficulty(), bodyFont));
            document.add(new Paragraph("Number of Questions: " + report.getNumberOfQuestions(), bodyFont));
            document.add(new Paragraph("Score: " + report.getScore() + "/" + report.getNumberOfQuestions(), bodyFont));
            document.add(new Paragraph("Accuracy Percentage: " + report.getAccuracyPercentage() + "%", bodyFont));
            document.add(new Paragraph("Date & Time: " + report.getTakenAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), bodyFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("2) Answer Review Section", sectionFont));
            PdfPTable table = new PdfPTable(new float[]{4, 3, 3, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            table.addCell(headerCell("Question"));
            table.addCell(headerCell("Selected Answer"));
            table.addCell(headerCell("Correct Answer"));
            table.addCell(headerCell("Status"));
            for (Map<String, Object> item : answerRows) {
                table.addCell(valueCell(String.valueOf(item.getOrDefault("question", "-"))));
                table.addCell(valueCell(String.valueOf(item.getOrDefault("selectedAnswer", "Not answered"))));
                table.addCell(valueCell(String.valueOf(item.getOrDefault("correctAnswer", "-"))));
                table.addCell(valueCell(String.valueOf(item.getOrDefault("status", "-"))));
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("3) Performance Summary", sectionFont));
            addBulletList(document, performanceSummary, bodyFont);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("4) Improvement Suggestions", sectionFont));
            addBulletList(document, improvementSuggestions, bodyFont);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate PDF report.", ex);
        }
    }

    private void validateSaveRequest(SaveQuizResultRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (request.getScore() == null || request.getScore() < 0) {
            throw new IllegalArgumentException("score must be zero or greater.");
        }
        if (request.getTotalQuestions() == null || request.getTotalQuestions() <= 0) {
            throw new IllegalArgumentException("totalQuestions must be greater than zero.");
        }
        if (request.getScore() > request.getTotalQuestions()) {
            throw new IllegalArgumentException("score cannot be greater than totalQuestions.");
        }
    }

    private void validateReportRequest(SaveQuizResultReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Report body is required.");
        }
        if (!StringUtils.hasText(request.getTopic())) {
            throw new IllegalArgumentException("Quiz topic is required.");
        }
        if (!StringUtils.hasText(request.getDifficulty())) {
            throw new IllegalArgumentException("Difficulty level is required.");
        }
        if (request.getNumberOfQuestions() == null || request.getNumberOfQuestions() <= 0) {
            throw new IllegalArgumentException("numberOfQuestions must be greater than zero.");
        }
        if (request.getScore() == null || request.getScore() < 0 || request.getScore() > request.getNumberOfQuestions()) {
            throw new IllegalArgumentException("score is invalid.");
        }
        if (request.getAccuracyPercentage() == null || request.getAccuracyPercentage() < 0 || request.getAccuracyPercentage() > 100) {
            throw new IllegalArgumentException("accuracyPercentage must be between 0 and 100.");
        }
        if (request.getAnswerReview() == null || request.getAnswerReview().isEmpty()) {
            throw new IllegalArgumentException("Answer review data is required.");
        }
        if (request.getPerformanceSummary() == null || request.getPerformanceSummary().isEmpty()) {
            throw new IllegalArgumentException("Performance summary is required.");
        }
        if (request.getImprovementSuggestions() == null || request.getImprovementSuggestions().isEmpty()) {
            throw new IllegalArgumentException("Improvement suggestions are required.");
        }
    }

    private PdfPCell headerCell(String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBackgroundColor(new Color(30, 58, 138));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell valueCell(String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(6);
        return cell;
    }

    private void addBulletList(Document document, List<String> items, Font font) throws DocumentException {
        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        list.setListSymbol("\u2022 ");
        for (String item : items) {
            list.add(new ListItem(item, font));
        }
        document.add(list);
    }
}
