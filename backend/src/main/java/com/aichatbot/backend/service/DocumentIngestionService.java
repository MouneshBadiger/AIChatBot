package com.aichatbot.backend.service;

import com.aichatbot.backend.config.RagProperties;
import com.aichatbot.backend.model.DocumentChunkRequest;
import com.aichatbot.backend.model.DocumentUploadResponse;
import com.aichatbot.backend.model.UploadedDocumentSummary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentIngestionService {

    private final RagProperties ragProperties;
    private final DocumentEmbeddingService documentEmbeddingService;
    private final DocumentVectorStoreRepository vectorStoreRepository;

    public DocumentIngestionService(
            RagProperties ragProperties,
            DocumentEmbeddingService documentEmbeddingService,
            DocumentVectorStoreRepository vectorStoreRepository) {
        this.ragProperties = ragProperties;
        this.documentEmbeddingService = documentEmbeddingService;
        this.vectorStoreRepository = vectorStoreRepository;
    }

    public DocumentUploadResponse ingest(MultipartFile[] files) throws IOException {
        List<UploadedDocumentSummary> summaries = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String documentName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : file.getName();
            ExtractedDocument extractedDocument = extract(file, documentName);
            List<DocumentChunkRequest> chunkRequests = chunk(extractedDocument).stream()
                    .map(chunk -> new DocumentChunkRequest(
                            documentName,
                            chunk.section(),
                            chunk.pageNumber(),
                            chunk.content(),
                            documentEmbeddingService.embed(chunk.content())))
                    .toList();
            vectorStoreRepository.saveAll(chunkRequests);
            summaries.add(new UploadedDocumentSummary(documentName, chunkRequests.size()));
        }
        return new DocumentUploadResponse(summaries);
    }

    private ExtractedDocument extract(MultipartFile file, String documentName) throws IOException {
        String extension = FilenameUtils.getExtension(documentName).toLowerCase();
        return switch (extension) {
            case "txt", "md", "markdown" -> new ExtractedDocument(List.of(new ExtractedSection("Body", null,
                    new String(file.getBytes(), StandardCharsets.UTF_8))));
            case "pdf" -> extractPdf(file);
            case "docx" -> extractDocx(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };
    }

    private ExtractedDocument extractPdf(MultipartFile file) throws IOException {
        List<ExtractedSection> sections = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); var pdf = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                sections.add(new ExtractedSection("Page " + page, page, stripper.getText(pdf)));
            }
        }
        return new ExtractedDocument(sections);
    }

    private ExtractedDocument extractDocx(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream(); XWPFDocument document = new XWPFDocument(inputStream)) {
            List<ExtractedSection> sections = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            String currentHeading = "Body";
            for (var paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (!StringUtils.hasText(text)) {
                    continue;
                }
                if (paragraph.getStyle() != null && paragraph.getStyle().toLowerCase().contains("heading")) {
                    if (current.length() > 0) {
                        sections.add(new ExtractedSection(currentHeading, null, current.toString()));
                        current.setLength(0);
                    }
                    currentHeading = text;
                }
                else {
                    current.append(text).append("\n");
                }
            }
            if (current.length() > 0) {
                sections.add(new ExtractedSection(currentHeading, null, current.toString()));
            }
            if (sections.isEmpty()) {
                sections.add(new ExtractedSection("Body", null, document.getParagraphs().stream().map(p -> p.getText() + "\n").reduce("", String::concat)));
            }
            return new ExtractedDocument(sections);
        }
    }

    private List<ExtractedSection> chunk(ExtractedDocument extractedDocument) {
        List<ExtractedSection> chunks = new ArrayList<>();
        int chunkSize = ragProperties.chunkSize();
        int overlap = ragProperties.chunkOverlap();
        for (ExtractedSection section : extractedDocument.sections()) {
            String normalized = section.content().replaceAll("\\s+", " ").trim();
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            for (int start = 0; start < normalized.length(); start += Math.max(1, chunkSize - overlap)) {
                int end = Math.min(normalized.length(), start + chunkSize);
                chunks.add(new ExtractedSection(section.section(), section.pageNumber(), normalized.substring(start, end)));
                if (end == normalized.length()) {
                    break;
                }
            }
        }
        return chunks;
    }

    private record ExtractedDocument(List<ExtractedSection> sections) {
    }

    private record ExtractedSection(String section, Integer pageNumber, String content) {
    }
}
