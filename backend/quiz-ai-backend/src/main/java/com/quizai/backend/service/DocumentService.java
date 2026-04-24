package com.quizai.backend.service;

import com.quizai.backend.dto.document.DocumentDetailsResponse;
import com.quizai.backend.model.Document;
import com.quizai.backend.model.DocumentChunk;
import com.quizai.backend.repository.DocumentChunkRepository;
import com.quizai.backend.repository.DocumentRepository;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private static final int CHUNK_SIZE = 800;

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentService(DocumentRepository documentRepository, DocumentChunkRepository documentChunkRepository) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public Document uploadDocument(MultipartFile file) {
        validateFile(file);

        // Delete old uploaded documents first
        documentChunkRepository.deleteAll();
        documentRepository.deleteAll();

        String extractedText = extractText(file);
        List<String> chunks = splitIntoChunks(extractedText);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No text could be extracted from document.");
        }

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        document.setFileSize(file.getSize());

        Document savedDocument = documentRepository.save(document);

        List<DocumentChunk> chunkEntities = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(savedDocument.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunkEntities.add(chunk);
        }

        documentChunkRepository.saveAll(chunkEntities);

        return savedDocument;
    }
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public DocumentDetailsResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found."));
        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(id);
        return new DocumentDetailsResponse(document, chunks);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("File name is missing.");
        }
        String lower = name.toLowerCase();
        if (!isSupportedExtension(lower)) {
            throw new IllegalArgumentException("Unsupported file type. Supported files: PDF, TXT, DOC, DOCX, PPT, PPTX.");
        }
    }

    private String extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (fileName.endsWith(".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                    return new PDFTextStripper().getText(pdf);
                }
            }
            if (fileName.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
            if (fileName.endsWith(".docx")) {
                return extractDocxText(file);
            }
            if (fileName.endsWith(".doc")) {
                return extractDocText(file);
            }
            if (fileName.endsWith(".pptx")) {
                return extractPptxText(file);
            }
            if (fileName.endsWith(".ppt")) {
                return extractPptText(file);
            }
            throw new IllegalArgumentException("Unsupported file type. Supported files: PDF, TXT, DOC, DOCX, PPT, PPTX.");
        } catch (IOException ex) {
            throw new IllegalStateException("We could not extract text from this file. Please verify the file is valid and try again.", ex);
        }
    }

    private String extractDocText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDocxText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractPptText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             HSLFSlideShow slideShow = new HSLFSlideShow(inputStream);
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(slideShow)) {
            return extractor.getText();
        }
    }

    private String extractPptxText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XMLSlideShow slideShow = new XMLSlideShow(inputStream)) {
            StringBuilder textBuilder = new StringBuilder();
            slideShow.getSlides().forEach(slide -> slide.getShapes().forEach(shape -> {
                if (shape instanceof XSLFTextShape textShape) {
                    String current = textShape.getText();
                    if (StringUtils.hasText(current)) {
                        textBuilder.append(current).append('\n');
                    }
                }
            }));
            return textBuilder.toString();
        }
    }

    private boolean isSupportedExtension(String fileNameLowercase) {
        return fileNameLowercase.endsWith(".pdf")
                || fileNameLowercase.endsWith(".txt")
                || fileNameLowercase.endsWith(".doc")
                || fileNameLowercase.endsWith(".docx")
                || fileNameLowercase.endsWith(".ppt")
                || fileNameLowercase.endsWith(".pptx");
    }

    private List<String> splitIntoChunks(String text) {
        String normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            chunks.add(normalized.substring(start, end).trim());
            start = end;
        }
        return chunks;
    }
}
