package com.aichatbot.backend.service;

import com.aichatbot.backend.config.GeminiProperties;
import com.aichatbot.backend.model.ChatError;
import com.aichatbot.backend.model.ChatMetadata;
import com.aichatbot.backend.model.ChatRequest;
import com.aichatbot.backend.model.ChatResponse;
import com.aichatbot.backend.model.ChatSource;
import com.aichatbot.backend.model.DocumentChunk;
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
    private final DocumentRetrievalService documentRetrievalService;

    public GeminiChatService(
            RestClient.Builder restClientBuilder,
            GeminiProperties geminiProperties,
            DocumentRetrievalService documentRetrievalService) {
        this.restClient = restClientBuilder.build();
        this.geminiProperties = geminiProperties;
        this.documentRetrievalService = documentRetrievalService;
    }

    public ChatResponse chat(ChatRequest chatRequest) {
        List<DocumentChunk> retrievedChunks = documentRetrievalService.retrieveRelevantChunks(chatRequest.message());
        ChatMetadata metadata = new ChatMetadata(
                geminiProperties.model(),
                Instant.now(),
                chatRequest.message().length());

        if (!StringUtils.hasText(geminiProperties.apiKey())) {
            return new ChatResponse(
                    null,
                    metadata,
                    mapSources(retrievedChunks),
                    new ChatError("missing_api_key", "Gemini API key is not configured."));
        }

        try {
            String groundedPrompt = buildGroundedPrompt(chatRequest.message(), retrievedChunks);
            GeminiGenerateContentRequest request = new GeminiGenerateContentRequest(
                    List.of(new GeminiContent(List.of(new GeminiPart(groundedPrompt)))));
            GeminiGenerateContentResponse response = restClient.post()
                    .uri(getGenerateContentUri())
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            String reply = extractReply(response);
            if (!StringUtils.hasText(reply)) {
                return new ChatResponse(
                        null,
                        metadata,
                        mapSources(retrievedChunks),
                        new ChatError("empty_reply", "Gemini returned an empty response."));
            }

            return new ChatResponse(reply, metadata, mapSources(retrievedChunks), null);
        }
        catch (RestClientResponseException ex) {
            return new ChatResponse(
                    null,
                    metadata,
                    mapSources(retrievedChunks),
                    new ChatError(buildHttpErrorCode(ex.getStatusCode()), ex.getResponseBodyAsString()));
        }
        catch (Exception ex) {
            return new ChatResponse(
                    null,
                    metadata,
                    mapSources(retrievedChunks),
                    new ChatError("gemini_request_failed", ex.getMessage()));
        }
    }

    private String buildGroundedPrompt(String userMessage, List<DocumentChunk> retrievedChunks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a document-grounded assistant. Answer only from the provided excerpts. ")
                .append("If the answer is not contained in the excerpts, say you do not have enough information.\n\n")
                .append("User question: ").append(userMessage).append("\n\n")
                .append("Relevant excerpts:\n");

        for (int i = 0; i < retrievedChunks.size(); i++) {
            DocumentChunk chunk = retrievedChunks.get(i);
            prompt.append(i + 1).append(". Document: ").append(chunk.documentName())
                    .append(", section: ").append(chunk.section());
            if (chunk.pageNumber() != null) {
                prompt.append(", page: ").append(chunk.pageNumber());
            }
            prompt.append("\n").append(chunk.content()).append("\n\n");
        }
        prompt.append("Cite the supporting document names in your answer when possible.");
        return prompt.toString();
    }

    private List<ChatSource> mapSources(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(chunk -> new ChatSource(
                        chunk.documentName(),
                        chunk.section(),
                        chunk.pageNumber(),
                        chunk.content(),
                        chunk.similarityScore()))
                .toList();
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
