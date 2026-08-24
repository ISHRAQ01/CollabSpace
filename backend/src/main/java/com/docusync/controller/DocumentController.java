package com.docusync.controller;

import com.docusync.dto.document.DocumentCreateRequest;
import com.docusync.dto.document.DocumentResponse;
import com.docusync.dto.document.DocumentUpdateRequest;
import com.docusync.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Document Controller
 * 
 * REST endpoints for document management
 */
@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    /**
     * Create new document
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentCreateRequest request) {
        
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.createDocument(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get document by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.getDocument(documentId, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update document metadata
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody DocumentUpdateRequest request) {
        
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.updateDocument(
                documentId, request, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update document content
     */
    @PatchMapping("/{documentId}/content")
    public ResponseEntity<DocumentResponse> updateDocumentContent(
            @PathVariable UUID documentId,
            @RequestBody String content) {
        
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.updateDocumentContent(
                documentId, content, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all user documents
     */
    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getUserDocuments(
            @PageableDefault(size = 20, sort = "updatedAt", 
                    direction = Sort.Direction.DESC) Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        Page<DocumentResponse> documents = documentService.getUserDocuments(
                userId, pageable);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Search documents
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        Page<DocumentResponse> documents = documentService.searchUserDocuments(
                userId, query, pageable);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Get recent documents
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentResponse>> getRecentDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        
        UUID userId = getCurrentUserId();
        List<DocumentResponse> documents = documentService.getRecentDocuments(
                userId, limit);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        
        // Extract user ID from JWT token or authentication principal
        // In production, use a custom UserPrincipal or extract from JWT
        String username = authentication.getName();
        
        // For now, this is a placeholder
        // Will be replaced with proper user ID extraction
        return UUID.randomUUID();
    }
}