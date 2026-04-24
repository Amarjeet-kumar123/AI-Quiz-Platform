package com.quizai.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizai.backend.dto.AnswerExplanationItemRequest;
import com.quizai.backend.dto.AnswerExplanationItemResponse;
import com.quizai.backend.dto.AnswerExplanationRequest;
import com.quizai.backend.dto.AnswerExplanationResponse;
import com.quizai.backend.dto.GenerateQuizRequest;
import com.quizai.backend.dto.GeneratedQuizResponse;
import com.quizai.backend.model.Question;
import com.quizai.backend.model.Quiz;
import com.quizai.backend.repository.QuestionRepository;
import com.quizai.backend.repository.QuizRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class AIQuizService {

    private static final Logger log = LoggerFactory.getLogger(AIQuizService.class);

    private static final String GEMINI_API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final RetrievalService retrievalService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String geminiApiKey;
    private final String geminiModel;

    public AIQuizService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            RetrievalService retrievalService,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String geminiApiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String geminiModel
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.retrievalService = retrievalService;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    @Transactional
    public GeneratedQuizResponse generateQuiz(GenerateQuizRequest request) {

        validateRequest(request);

        Quiz quiz = new Quiz();
        quiz.setTopic(request.getTopic().trim());
        quiz.setDifficulty(request.getDifficulty().trim());

        Quiz savedQuiz = quizRepository.save(quiz);

        List<Question> generatedQuestions = generateQuestionsWithAI(
                request.getTopic(),
                request.getDifficulty(),
                request.getNumberOfQuestions()
        );

        generatedQuestions.forEach(q -> q.setQuizId(savedQuiz.getId()));

        List<Question> savedQuestions = questionRepository.saveAll(generatedQuestions);

        return new GeneratedQuizResponse(savedQuiz.getId(), savedQuestions);
    }

    public List<Question> generateQuestionsWithAI(String topic, String difficulty, int numberOfQuestions) {

     if (!StringUtils.hasText(geminiApiKey)) {
    log.warn("Gemini API key missing -> using fallback quiz generation");
    return getFallbackQuestions(topic, difficulty, numberOfQuestions);
}

        String prompt = "Generate " + numberOfQuestions + " multiple choice quiz questions about " + topic
                + " with " + difficulty + " difficulty. Each question must have optionA, optionB, optionC, optionD, and correctAnswer. Return JSON format.";

        List<String> retrievedChunks = retrievalService.retrieveRelevantChunks(topic);
        String contextBlock = "";
        if (!retrievedChunks.isEmpty()) {
            contextBlock = "Context:\n" + String.join("\n---\n", retrievedChunks) + "\n\n";
        }

        String strictPrompt = contextBlock + prompt + " Return only a JSON array. No markdown, no extra text.";


        Map<String, Object> part = Map.of("text", strictPrompt);
        Map<String, Object> content = Map.of("parts", List.of(part));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(content));
       

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(payload, headers);

        String selectedModel = resolveGeminiModel();
        log.info("Using Gemini model: {}", selectedModel);
        String url = GEMINI_API_URL_TEMPLATE.formatted(selectedModel, geminiApiKey);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, requestEntity, String.class);
        }catch (HttpStatusCodeException ex) {
    log.warn("Gemini unavailable -> using fallback quiz generation. status={}, body={}",
            ex.getStatusCode(),
            ex.getResponseBodyAsString());

    return getFallbackQuestions(topic, difficulty, numberOfQuestions);
}
        HttpStatusCode status = response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            throw new IllegalStateException("Gemini API returned non-success status: " + status);
        }
        if (!StringUtils.hasText(response.getBody())) {
            throw new IllegalStateException("Gemini API returned empty response body.");
        }

        try {

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini returned no candidates.");
            }

            JsonNode parts = candidates.path(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new IllegalStateException("Gemini returned no content parts.");
            }

            String text = "";
            for (JsonNode partNode : parts) {
                if (partNode.hasNonNull("text")) {
                    text += partNode.get("text").asText();
                }
            }
            if (!StringUtils.hasText(text)) {
                throw new IllegalStateException("Gemini response text is empty.");
            }

            String normalizedJson = sanitizeJson(text);
            List<Map<String, Object>> aiQuestions =
                    objectMapper.readValue(normalizedJson, new TypeReference<>() {});
            if (aiQuestions.isEmpty()) {
                throw new IllegalStateException("Gemini returned empty question list.");
            }

            List<Question> questions = new ArrayList<>();

            for (Map<String, Object> q : aiQuestions) {

                Question question = new Question();

                question.setQuestion(requireField(q, "question"));
                question.setOptionA(requireField(q, "optionA"));
                question.setOptionB(requireField(q, "optionB"));
                question.setOptionC(requireField(q, "optionC"));
                question.setOptionD(requireField(q, "optionD"));
                question.setCorrectAnswer(requireField(q, "correctAnswer"));

                questions.add(question);
            }

            return questions;

        } catch (Exception e) {

            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }
    // Add this full method inside AIQuizService.java
// preferably below generateQuestionsWithAI(...)

private List<Question> getFallbackQuestions(String topic, String difficulty, int numberOfQuestions) {

    List<Question> fallbackQuestions = new ArrayList<>();

    List<Question> javaQuestions = List.of(
            createQuestion(
                    "What does JVM stand for?",
                    "Java Virtual Machine",
                    "Java Variable Method",
                    "Joint Virtual Memory",
                    "Java Verified Module",
                    "Java Virtual Machine"
            ),
            createQuestion(
                    "Which concept allows one object to take many forms?",
                    "Encapsulation",
                    "Polymorphism",
                    "Inheritance",
                    "Abstraction",
                    "Polymorphism"
            ),
            createQuestion(
                    "Which keyword is used for inheritance in Java?",
                    "extends",
                    "implements",
                    "inherits",
                    "super",
                    "extends"
            ),
            createQuestion(
                    "Which block is used to handle exceptions?",
                    "if block",
                    "catch block",
                    "switch block",
                    "loop block",
                    "catch block"
            ),
            createQuestion(
                    "Which collection stores unique values only?",
                    "List",
                    "ArrayList",
                    "Set",
                    "Vector",
                    "Set"
            )
    );

    List<Question> genericQuestions = List.of(
            createQuestion(
                    "What is the main purpose of a database?",
                    "Store and manage data",
                    "Create images",
                    "Compile code",
                    "Run hardware",
                    "Store and manage data"
            ),
            createQuestion(
                    "Which SQL command is used to fetch data?",
                    "INSERT",
                    "UPDATE",
                    "SELECT",
                    "DELETE",
                    "SELECT"
            ),
            createQuestion(
                    "What does CPU stand for?",
                    "Central Process Unit",
                    "Central Processing Unit",
                    "Computer Personal Unit",
                    "Control Processing Utility",
                    "Central Processing Unit"
            ),
            createQuestion(
                    "Which OS scheduling algorithm works on shortest job first?",
                    "FCFS",
                    "SJF",
                    "Round Robin",
                    "Priority",
                    "SJF"
            ),
            createQuestion(
                    "Which network device connects multiple networks?",
                    "Switch",
                    "Hub",
                    "Router",
                    "Repeater",
                    "Router"
            )
    );

    List<Question> selectedPool;

    if (topic != null && topic.toLowerCase().contains("java")) {
        selectedPool = javaQuestions;
    } else {
        selectedPool = genericQuestions;
    }

    for (int i = 0; i < numberOfQuestions; i++) {
        fallbackQuestions.add(selectedPool.get(i % selectedPool.size()));
    }

    log.info("Fallback quiz generated successfully for topic: {}", topic);

    return fallbackQuestions;
}
// Add this helper method inside AIQuizService.java

private Question createQuestion(
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer
) {
    Question question = new Question();

    question.setQuestion(questionText);
    question.setOptionA(optionA);
    question.setOptionB(optionB);
    question.setOptionC(optionC);
    question.setOptionD(optionD);
    question.setCorrectAnswer(correctAnswer);

    return question;
}

    public AnswerExplanationResponse generateAnswerExplanations(AnswerExplanationRequest request) {
        validateExplanationRequest(request);

        if (!StringUtils.hasText(geminiApiKey)) {
            return fallbackExplanationResponse(request.getItems());
        }

        try {
            String strictPrompt = buildExplanationPrompt(request);
            Map<String, Object> payload = buildGeminiPayload(strictPrompt);
            String text = callGemini(payload);
            String normalizedJson = sanitizeJson(text);

            List<AnswerExplanationItemResponse> items =
                    objectMapper.readValue(normalizedJson, new TypeReference<>() {});
            if (items == null || items.isEmpty()) {
                return fallbackExplanationResponse(request.getItems());
            }

            Map<Integer, AnswerExplanationItemResponse> parsedByIndex = new HashMap<>();
            for (AnswerExplanationItemResponse item : items) {
                if (item.getQuestionIndex() != null) {
                    parsedByIndex.put(item.getQuestionIndex(), item);
                }
            }

            List<AnswerExplanationItemResponse> normalized = new ArrayList<>();
            for (AnswerExplanationItemRequest sourceItem : request.getItems()) {
                AnswerExplanationItemResponse parsed = parsedByIndex.get(sourceItem.getQuestionIndex());
                if (parsed == null
                        || !StringUtils.hasText(parsed.getExplanation())
                        || !StringUtils.hasText(parsed.getWrongOptionExplanation())) {
                    normalized.add(fallbackForItem(sourceItem));
                } else {
                    normalized.add(new AnswerExplanationItemResponse(
                            sourceItem.getQuestionIndex(),
                            parsed.getExplanation().trim(),
                            parsed.getWrongOptionExplanation().trim()
                    ));
                }
            }
            return new AnswerExplanationResponse(normalized);
        } catch (Exception ex) {
            log.warn("Gemini explanation generation failed. Using fallback. reason={}", ex.getMessage());
            return fallbackExplanationResponse(request.getItems());
        }
    }

    private String requireField(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null || !StringUtils.hasText(value.toString())) {
            throw new IllegalStateException("Gemini response missing required field: " + key);
        }
        return value.toString().trim();
    }

    private String sanitizeJson(String raw) {
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceAll("^```json\\s*", "");
            text = text.replaceAll("^```\\s*", "");
            text = text.replaceAll("\\s*```$", "");
        }

        // Fallback for accidental extra text around JSON array
        int arrayStart = text.indexOf('[');
        int arrayEnd = text.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return text.substring(arrayStart, arrayEnd + 1).trim();
        }
        return text;
    }

    private String callGemini(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        String selectedModel = resolveGeminiModel();
        String url = GEMINI_API_URL_TEMPLATE.formatted(selectedModel, geminiApiKey);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, requestEntity, String.class);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Gemini API request failed. status=" + ex.getStatusCode() + ", body=" + ex.getResponseBodyAsString(), ex);
        }

        HttpStatusCode status = response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            throw new IllegalStateException("Gemini API returned non-success status: " + status);
        }
        if (!StringUtils.hasText(response.getBody())) {
            throw new IllegalStateException("Gemini API returned empty response body.");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini returned no candidates.");
            }

            JsonNode parts = candidates.path(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new IllegalStateException("Gemini returned no content parts.");
            }

            StringBuilder text = new StringBuilder();
            for (JsonNode partNode : parts) {
                if (partNode.hasNonNull("text")) {
                    text.append(partNode.get("text").asText());
                }
            }
            if (!StringUtils.hasText(text.toString())) {
                throw new IllegalStateException("Gemini response text is empty.");
            }
            return text.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Gemini response.", ex);
        }
    }

    private Map<String, Object> buildGeminiPayload(String promptText) {
        Map<String, Object> part = Map.of("text", promptText);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(content));
        return payload;
    }

    private String buildExplanationPrompt(AnswerExplanationRequest request) throws Exception {
        String requestJson = objectMapper.writeValueAsString(request.getItems());
        return "You are an educational quiz tutor. "
                + "Generate concise explanation output in strict JSON array format only.\n"
                + "Topic: " + request.getTopic() + "\n"
                + "Difficulty: " + request.getDifficulty() + "\n"
                + "For each item, return exactly these keys: questionIndex, explanation, wrongOptionExplanation.\n"
                + "Rules:\n"
                + "- explanation: 1-2 short sentences on why the correct answer is correct.\n"
                + "- wrongOptionExplanation: 1 short sentence on why selected/other options are incorrect.\n"
                + "- Keep wording simple, educational, and under 35 words each field.\n"
                + "- Preserve each questionIndex.\n"
                + "Input JSON:\n"
                + requestJson
                + "\nReturn only JSON array.";
    }

    private void validateExplanationRequest(AnswerExplanationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        if (!StringUtils.hasText(request.getTopic())) {
            throw new IllegalArgumentException("Topic is required.");
        }
        if (!StringUtils.hasText(request.getDifficulty())) {
            throw new IllegalArgumentException("Difficulty is required.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one question item is required.");
        }
    }

    private AnswerExplanationResponse fallbackExplanationResponse(List<AnswerExplanationItemRequest> sourceItems) {
        List<AnswerExplanationItemResponse> items = sourceItems.stream()
                .map(this::fallbackForItem)
                .toList();
        return new AnswerExplanationResponse(items);
    }

    private AnswerExplanationItemResponse fallbackForItem(AnswerExplanationItemRequest sourceItem) {
        String explanation = "The correct answer best matches the concept tested in this question.";
        String wrongExplanation = "Other options do not satisfy the core requirement asked here.";
        if ("Correct".equalsIgnoreCase(sourceItem.getStatus())) {
            wrongExplanation = "Your selected answer is correct; other options are less accurate for this concept.";
        } else if (StringUtils.hasText(sourceItem.getSelectedAnswer())) {
            wrongExplanation = "Your selected answer is related but does not fully match the expected concept.";
        }
        return new AnswerExplanationItemResponse(
                sourceItem.getQuestionIndex(),
                explanation,
                wrongExplanation
        );
    }

    private String resolveGeminiModel() {
        String configuredModel = StringUtils.hasText(geminiModel) ? geminiModel.trim() : "";
        if (!StringUtils.hasText(configuredModel) || configuredModel.equalsIgnoreCase("gemini-pro")) {
            log.warn("Configured gemini.model='{}' is unsupported for this endpoint. Falling back to gemini-2.5-flash", configuredModel);
            return "gemini-2.5-flash";
        }
        return configuredModel;
    }

    private void validateRequest(GenerateQuizRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }

        if (!StringUtils.hasText(request.getTopic())) {
            throw new IllegalArgumentException("Topic is required.");
        }

        if (!StringUtils.hasText(request.getDifficulty())) {
            throw new IllegalArgumentException("Difficulty is required.");
        }

        if (request.getNumberOfQuestions() <= 0) {
            throw new IllegalArgumentException("Number of questions must be greater than 0.");
        }

        if (request.getNumberOfQuestions() > 20) {
            throw new IllegalArgumentException("Max 20 questions allowed.");
        }
    }
}