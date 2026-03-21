package com.aichatbot.backend.model;

public record ChatResponse(String reply, ChatMetadata metadata, ChatError error) {
}
