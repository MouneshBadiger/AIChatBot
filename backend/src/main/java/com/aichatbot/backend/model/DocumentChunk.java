package com.aichatbot.backend.model;

public record DocumentChunk(
        Long id,
        String documentName,
        String section,
        Integer pageNumber,
        String content,
        double similarityScore) {
}
