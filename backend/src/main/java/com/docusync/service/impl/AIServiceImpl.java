package com.docusync.service.impl;

import com.docusync.dto.ai.AIQueryRequest;
import com.docusync.dto.ai.AIQueryResponse;
import com.docusync.exception.AIServiceException;
import com.docusync.exception.AuthorizationException;
import com.docusync.exception.ResourceNotFoundException;
import com.docusync.entity.Document;
import com.docusync.entity.DocumentCollaborator;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.repository.DocumentCollaboratorRepository;
import com.docusync.repository.DocumentRepository;
import com.docusync.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI Service Implementation
 * 
 * Handles AI-powered document operations with permission checks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final RagEngineService ragEngineService;
    private final DocumentRepository documentRepository;
    private final DocumentCollaboratorRepository collaboratorRepository;
    
    /**
     * Query document with AI
     */
    @Override
    @Transactional(readOnly = true)
    public AIQueryResponse queryDocument(AIQueryRequest request, UUID userId) {
        log.info("AI query for document {} by user {}", 
                request.getDocumentId(), userId);
        
        // Verify access
        verifyDocumentAccess(request.getDocumentId(), userId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            String answer;
            int tokensUsed = 0;
            
            if (request.getUseContext()) {
                // Use RAG for context-aware query
                answer = ragEngineService.queryWithContext(
                        request.getDocumentId(), 
                        request.getPrompt());
            } else {
                // Direct query without context
                answer = ragEngineService.queryWithContext(
                        request.getDocumentId(), 
                        request.getPrompt());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return AIQueryResponse.builder()
                    .documentId(request.getDocumentId())
                    .answer(answer)
                    .tokensUsed(tokensUsed)
                    .processingTimeMs(processingTime)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("AI query failed: {}", e.getMessage(), e);
            throw new AIServiceException("query", e.getMessage());
        }
    }
    
    /**
     * Generate document summary
     */
    @Override
    @Transactional(readOnly = true)
    public String generateSummary(UUID documentId, UUID userId) {
        log.info("Generating summary for document {}", documentId);
        
        verifyDocumentAccess(documentId, userId);
        
        try {
            return ragEngineService.generateSummary(documentId);
        } catch (Exception e) {
            log.error("Summary generation failed: {}", e.getMessage(), e);
            throw new AIServiceException("summarize", e.getMessage());
        }
    }
    
    /**
     * Generate action items
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> generateActionItems(UUID documentId, UUID userId) {
        log.info("Generating action items for document {}", documentId);
        
        verifyDocumentAccess(documentId, userId);
        
        try {
            return ragEngineService.generateActionItems(documentId);
        } catch (Exception e) {
            log.error("Action items generation failed: {}", e.getMessage(), e);
            throw new AIServiceException("action-items", e.getMessage());
        }
    }
    
    /**
     * Improve writing
     */
    @Override
    @Transactional(readOnly = true)
    public String improveWriting(UUID documentId, String content, UUID userId) {
        log.debug("Improving writing for document {}", documentId);
        
        verifyDocumentAccess(documentId, userId);
        
        try {
            return ragEngineService.improveWriting(content);
        } catch (Exception e) {
            log.error("Writing improvement failed: {}", e.getMessage(), e);
            throw new AIServiceException("improve-writing", e.getMessage());
        }
    }
    
    /**
     * Index document for RAG
     */
    @Override
    @Async
    @Transactional
    public void indexDocument(UUID documentId, UUID userId) {
        log.info("Indexing document {} for RAG", documentId);
        
        verifyDocumentAccess(documentId, userId);
        
        try {
            ragEngineService.indexDocument(documentId);
        } catch (Exception e) {
            log.error("Document indexing failed: {}", e.getMessage(), e);
            throw new AIServiceException("index", e.getMessage());
        }
    }
    
    /**
     * Remove document from index
     */
    @Override
    @Transactional
    public void removeDocumentFromIndex(UUID documentId) {
        log.info("Removing document {} from RAG index", documentId);
        
        // Implementation depends on vector store
        // For now, this is a placeholder
    }
    
    /**
     * Verify user has access to document
     */
    private void verifyDocumentAccess(UUID documentId, UUID userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document", "id", documentId));
        
        // Check if user is creator
        if (document.getCreatedBy().getId().equals(userId)) {
            return;
        }
        
        // Check collaborator access
        collaboratorRepository.findByDocumentAndUser(documentId, userId)
                .filter(collaborator -> collaborator.getIsActive())
                .orElseThrow(() -> new AuthorizationException("Document", "access"));
    }
}