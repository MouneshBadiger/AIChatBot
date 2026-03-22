package com.aichatbot.backend.model;

import java.util.List;

public record DocumentUploadResponse(List<UploadedDocumentSummary> documents) {
}
