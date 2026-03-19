package com.aichatbot.backend.service;

import com.aichatbot.backend.model.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("UP", "Spring Boot backend is reachable.");
    }
}
