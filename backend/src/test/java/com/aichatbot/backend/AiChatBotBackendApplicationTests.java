package com.aichatbot.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiChatBotBackendApplicationTests {

    @Test
    @EnabledIfEnvironmentVariable(named = "SPRING_AI_OPENAI_API_KEY", matches = ".+")
    @EnabledIfSystemProperty(named = "spring.ai.openai.api-key", matches = ".+")
    void contextLoads() {
    }
}
