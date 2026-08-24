package com.docusync.service;

import com.docusync.dto.document.DocumentCreateRequest;
import com.docusync.dto.document.DocumentResponse;
import com.docusync.dto.document.DocumentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Document Service Interface
 * 
 * Defines business operations for document management
 */
public interface DocumentService {
    
    /**
     * Create a new document
     */
    DocumentResponse createDocument(DocumentCreateRequest request, UUID userId);
    
    /**
     * Get document by ID
     */
    DocumentResponse getDocument(UUID documentId, UUID userId);
    
    /**
     * Update document metadata
     */
    DocumentResponse updateDocument(UUID documentId, DocumentUpdateRequest request, UUID userId);
    
    /**
     * Update document content
     */
    DocumentResponse updateDocumentContent(UUID documentId, String content, UUID userId);
    
    /**
     * Delete (archive) document
     */
    void deleteDocument(UUID documentId, UUID userId);
    
    /**
     * Get all accessible documents for user
     */
    Page<DocumentResponse> getUserDocuments(UUID userId, Pageable pageable);
    
    /**
     * Search user's documents
     */
    Page<DocumentResponse> searchUserDocuments(UUID userId, String searchTerm, Pageable pageable);
    
    /**
     * Get recently updated documents
     */
    java.util.List<DocumentResponse> getRecentDocuments(UUID userId, int limit);
}