package com.aichatbot.backend.service;

import com.aichatbot.backend.config.GeminiProperties;
import com.aichatbot.backend.model.ChatError;
import com.aichatbot.backend.model.ChatMetadata;
import com.aichatbot.backend.model.ChatRequest;
import com.aichatbot.backend.model.ChatResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GeminiChatService {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;

    public GeminiChatService(RestClient.Builder restClientBuilder, GeminiProperties geminiProperties) {
        this.restClient = restClientBuilder.build();
        this.geminiProperties = geminiProperties;
    }

    public ChatResponse chat(ChatRequest chatRequest) {
        ChatMetadata metadata = new ChatMetadata(
                geminiProperties.model(),
                Instant.now(),
                chatRequest.message().length());

        if (!StringUtils.hasText(geminiProperties.apiKey())) {
            return new ChatResponse(
                    null,
                    metadata,
                    new ChatError("missing_api_key", "Gemini API key is not configured."));
        }

        try {
            GeminiGenerateContentResponse response = restClient.post()
                    .uri(getGenerateContentUri())
                    .body(new GeminiGenerateContentRequest(List.of(new GeminiContent(
                            List.of(new GeminiPart(chatRequest.message()))))))
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            String reply = extractReply(response);
            if (!StringUtils.hasText(reply)) {
                return new ChatResponse(
                        null,
                        metadata,
                        new ChatError("empty_reply", "Gemini returned an empty response."));
            }

            return new ChatResponse(reply, metadata, null);
        }
        catch (RestClientResponseException ex) {
            return new ChatResponse(
                    null,
                    metadata,
                    new ChatError(buildHttpErrorCode(ex.getStatusCode()), ex.getResponseBodyAsString()));
        }
        catch (Exception ex) {
            return new ChatResponse(
                    null,
                    metadata,
                    new ChatError("gemini_request_failed", ex.getMessage()));
        }
    }

    private String getGenerateContentUri() {
        String baseUrl = StringUtils.hasText(geminiProperties.baseUrl())
                ? geminiProperties.baseUrl()
                : DEFAULT_BASE_URL;
        return baseUrl + "/v1beta/models/" + geminiProperties.model() + ":generateContent?key="
                + geminiProperties.apiKey();
    }

    private String extractReply(GeminiGenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }

        GeminiCandidate firstCandidate = response.candidates().get(0);
        if (firstCandidate.content() == null || firstCandidate.content().parts() == null
                || firstCandidate.content().parts().isEmpty()) {
            return null;
        }

        return firstCandidate.content().parts().stream()
                .map(GeminiPart::text)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private String buildHttpErrorCode(HttpStatusCode statusCode) {
        return "gemini_http_" + statusCode.value();
    }

    private record GeminiGenerateContentRequest(List<GeminiContent> contents) {
    }

    private record GeminiGenerateContentResponse(List<GeminiCandidate> candidates) {
    }

    private record GeminiCandidate(GeminiContent content) {
    }

    private record GeminiContent(List<GeminiPart> parts) {
    }

    private record GeminiPart(String text) {
    }
}
