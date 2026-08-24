package com.docusync.controller;

import com.docusync.dto.ai.AIQueryRequest;
import com.docusync.dto.ai.AIQueryResponse;
import com.docusync.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI Controller
 * 
 * REST endpoints for AI-powered document operations
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    /**
     * Query document with AI
     */
    @PostMapping("/query")
    public ResponseEntity<AIQueryResponse> queryDocument(
            @Valid @RequestBody AIQueryRequest request) {
        
        UUID userId = getCurrentUserId();
        AIQueryResponse response = aiService.queryDocument(request, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate document summary
     */
    @PostMapping("/summarize/{documentId}")
    public ResponseEntity<String> generateSummary(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        String summary = aiService.generateSummary(documentId, userId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Generate action items
     */
    @PostMapping("/action-items/{documentId}")
    public ResponseEntity<List<String>> generateActionItems(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        List<String> actionItems = aiService.generateActionItems(
                documentId, userId);
        return ResponseEntity.ok(actionItems);
    }
    
    /**
     * Improve writing
     */
    @PostMapping("/improve/{documentId}")
    public ResponseEntity<String> improveWriting(
            @PathVariable UUID documentId,
            @RequestBody String content) {
        
        UUID userId = getCurrentUserId();
        String improvedText = aiService.improveWriting(
                documentId, content, userId);
        return ResponseEntity.ok(improvedText);
    }
    
    /**
     * Index document for RAG
     */
    @PostMapping("/index/{documentId}")
    public ResponseEntity<Void> indexDocument(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        aiService.indexDocument(documentId, userId);
        return ResponseEntity.accepted().build();
    }
    
    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        // Placeholder - will be replaced with proper implementation
        return UUID.randomUUID();
    }
}