package com.docusync.service;

import com.docusync.dto.ai.AIQueryRequest;
import com.docusync.dto.ai.AIQueryResponse;

import java.util.UUID;

/**
 * AI Service Interface
 * 
 * Defines operations for AI-powered document intelligence
 */
public interface AIService {
    
    /**
     * Query document with AI (RAG-based)
     */
    AIQueryResponse queryDocument(AIQueryRequest request, UUID userId);
    
    /**
     * Generate document summary
     */
    String generateSummary(UUID documentId, UUID userId);
    
    /**
     * Generate action items from document
     */
    java.util.List<String> generateActionItems(UUID documentId, UUID userId);
    
    /**
     * Improve writing for a section
     */
    String improveWriting(UUID documentId, String content, UUID userId);
    
    /**
     * Index document for RAG
     */
    void indexDocument(UUID documentId, UUID userId);
    
    /**
     * Remove document from index
     */
    void removeDocumentFromIndex(UUID documentId);
}