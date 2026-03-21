package com.aichatbot.backend.model;

import java.time.Instant;

public record ChatMetadata(String model, Instant timestamp, int promptLength) {
}
