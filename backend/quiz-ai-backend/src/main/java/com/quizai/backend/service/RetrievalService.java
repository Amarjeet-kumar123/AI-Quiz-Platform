package com.quizai.backend.service;

import com.quizai.backend.model.DocumentChunk;
import com.quizai.backend.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RetrievalService {

    private final DocumentChunkRepository documentChunkRepository;

    public RetrievalService(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    public List<String> retrieveRelevantChunks(String topicQuery) {
        if (!StringUtils.hasText(topicQuery)) {
            return List.of();
        }

        List<DocumentChunk> allChunks = documentChunkRepository.findAll();
        if (allChunks.isEmpty()) {
            return List.of();
        }

        List<String> queryTerms = Arrays.stream(topicQuery.toLowerCase().split("\\W+"))
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        return allChunks.stream()
                .map(chunk -> Map.entry(chunk.getContent(), scoreChunk(chunk.getContent(), queryTerms)))
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int scoreChunk(String content, List<String> queryTerms) {
        if (!StringUtils.hasText(content) || queryTerms.isEmpty()) {
            return 0;
        }
        String normalized = content.toLowerCase();
        int score = 0;
        for (String term : queryTerms) {
            if (normalized.contains(term)) {
                score += 1;
            }
        }
        return score;
    }
}
