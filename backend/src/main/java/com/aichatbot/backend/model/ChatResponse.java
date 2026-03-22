package com.aichatbot.backend.model;

import java.util.List;

public record ChatResponse(String reply, ChatMetadata metadata, List<ChatSource> sources, ChatError error) {
}
