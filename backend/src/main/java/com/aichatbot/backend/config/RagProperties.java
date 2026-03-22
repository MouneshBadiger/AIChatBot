package com.aichatbot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(int chunkSize, int chunkOverlap, int topK) {
}
