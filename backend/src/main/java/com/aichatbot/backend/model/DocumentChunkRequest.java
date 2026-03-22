package com.aichatbot.backend.model;

public record DocumentChunkRequest(
        String documentName,
        String section,
        Integer pageNumber,
        String content,
        float[] embedding) {
}
