package com.aichatbot.backend.service;

import com.aichatbot.backend.config.RagProperties;
import com.aichatbot.backend.model.DocumentChunk;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentRetrievalService {

    private final DocumentEmbeddingService documentEmbeddingService;
    private final DocumentVectorStoreRepository vectorStoreRepository;
    private final RagProperties ragProperties;

    public DocumentRetrievalService(
            DocumentEmbeddingService documentEmbeddingService,
            DocumentVectorStoreRepository vectorStoreRepository,
            RagProperties ragProperties) {
        this.documentEmbeddingService = documentEmbeddingService;
        this.vectorStoreRepository = vectorStoreRepository;
        this.ragProperties = ragProperties;
    }

    public List<DocumentChunk> retrieveRelevantChunks(String query) {
        float[] embedding = documentEmbeddingService.embed(query);
        return vectorStoreRepository.similaritySearch(embedding, ragProperties.topK());
    }
}
