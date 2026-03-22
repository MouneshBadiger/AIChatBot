package com.aichatbot.backend.model;

public record ChatSource(String documentName, String section, Integer pageNumber, String excerpt, double score) {
}
