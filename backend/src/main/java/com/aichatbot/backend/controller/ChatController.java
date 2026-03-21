package com.aichatbot.backend.controller;

import com.aichatbot.backend.model.ChatRequest;
import com.aichatbot.backend.model.ChatResponse;
import com.aichatbot.backend.service.GeminiChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GeminiChatService geminiChatService;

    public ChatController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest chatRequest) {
        ChatResponse response = geminiChatService.chat(chatRequest);
        HttpStatus status = response.error() == null ? HttpStatus.OK : HttpStatus.BAD_GATEWAY;
        if ("missing_api_key".equals(response.error() != null ? response.error().code() : null)) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(response);
    }
}
