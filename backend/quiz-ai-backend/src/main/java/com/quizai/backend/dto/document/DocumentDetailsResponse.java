package com.quizai.backend.dto.document;

import com.quizai.backend.model.Document;
import com.quizai.backend.model.DocumentChunk;

import java.util.List;

public class DocumentDetailsResponse {

    private Document document;
    private List<DocumentChunk> chunks;

    public DocumentDetailsResponse(Document document, List<DocumentChunk> chunks) {
        this.document = document;
        this.chunks = chunks;
    }

    public Document getDocument() {
        return document;
    }

    public List<DocumentChunk> getChunks() {
        return chunks;
    }
}
