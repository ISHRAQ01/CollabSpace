package com.docusync.service.impl;

import com.docusync.dto.document.DocumentCreateRequest;
import com.docusync.dto.document.DocumentResponse;
import com.docusync.dto.document.DocumentUpdateRequest;
import com.docusync.entity.Document;
import com.docusync.entity.DocumentCollaborator;
import com.docusync.entity.User;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.exception.AuthorizationException;
import com.docusync.exception.ResourceNotFoundException;
import com.docusync.repository.DocumentCollaboratorRepository;
import com.docusync.repository.DocumentRepository;
import com.docusync.repository.UserRepository;
import com.docusync.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Document Service Implementation
 * 
 * Handles document CRUD operations with permission checks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentCollaboratorRepository collaboratorRepository;
    
    /**
     * Create a new document
     */
    @Override
    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, UUID userId) {
        log.info("Creating document '{}' for user {}", request.getTitle(), userId);
        
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Document document = Document.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content("")  // Empty initial content
                .isPublic(request.getIsPublic())
                .isArchived(false)
                .currentVersion(1L)
                .createdBy(creator)
                .build();
        
        document = documentRepository.save(document);
        
        // Add creator as owner collaborator
        DocumentCollaborator collaborator = DocumentCollaborator.builder()
                .id(new DocumentCollaborator.DocumentCollaboratorId(
                        document.getId(), userId))
                .document(document)
                .user(creator)
                .role(CollaboratorRole.OWNER)
                .isActive(true)
                .build();
        
        collaboratorRepository.save(collaborator);
        
        log.info("Document created successfully: {}", document.getId());
        return mapToDocumentResponse(document, CollaboratorRole.OWNER);
    }
    
    /**
     * Get document by ID with permission check
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID documentId, UUID userId) {
        log.debug("Fetching document {} for user {}", documentId, userId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        CollaboratorRole userRole = getUserRole(documentId, userId, document);
        
        return mapToDocumentResponse(document, userRole);
    }
    
    /**
     * Update document metadata
     */
    @Override
    @Transactional
    public DocumentResponse updateDocument(
            UUID documentId, 
            DocumentUpdateRequest request, 
            UUID userId) {
        
        log.info("Updating document {} by user {}", documentId, userId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        // Check edit permissions
        CollaboratorRole userRole = getUserRole(documentId, userId, document);
        if (userRole != CollaboratorRole.OWNER && userRole != CollaboratorRole.EDITOR) {
            throw new AuthorizationException("Document", "edit");
        }
        
        // Update fields
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null && userRole == CollaboratorRole.OWNER) {
            document.setIsPublic(request.getIsPublic());
        }
        
        document = documentRepository.save(document);
        
        return mapToDocumentResponse(document, userRole);
    }
    
    /**
     * Update document content
     */
    @Override
    @Transactional
    public DocumentResponse updateDocumentContent(
            UUID documentId, 
            String content, 
            UUID userId) {
        
        log.debug("Updating content for document {} by user {}", documentId, userId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        // Check edit permissions
        CollaboratorRole userRole = getUserRole(documentId, userId, document);
        if (userRole != CollaboratorRole.OWNER && userRole != CollaboratorRole.EDITOR) {
            throw new AuthorizationException("Document", "edit");
        }
        
        // Update content and version
        documentRepository.updateDocumentContent(documentId, content);
        document = documentRepository.findById(documentId).get();
        
        return mapToDocumentResponse(document, userRole);
    }
    
    /**
     * Delete (archive) document
     */
    @Override
    @Transactional
    public void deleteDocument(UUID documentId, UUID userId) {
        log.info("Archiving document {} by user {}", documentId, userId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        
        // Only owner can delete document
        CollaboratorRole userRole = getUserRole(documentId, userId, document);
        if (userRole != CollaboratorRole.OWNER) {
            throw new AuthorizationException("Document", "delete");
        }
        
        documentRepository.archiveDocument(documentId);
    }
    
    /**
     * Get all accessible documents for user
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocuments(UUID userId, Pageable pageable) {
        log.debug("Fetching all documents for user {}", userId);
        
        Page<Document> documents = documentRepository.findAllAccessibleDocuments(userId, pageable);
        
        return documents.map(document -> {
            CollaboratorRole role = getUserRole(document.getId(), userId, document);
            return mapToDocumentResponse(document, role);
        });
    }
    
    /**
     * Search user's documents
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchUserDocuments(
            UUID userId, 
            String searchTerm, 
            Pageable pageable) {
        
        log.debug("Searching documents for user {} with term '{}'", userId, searchTerm);
        
        Page<Document> documents = documentRepository.searchDocuments(
                userId, searchTerm, pageable);
        
        return documents.map(document -> {
            CollaboratorRole role = getUserRole(document.getId(), userId, document);
            return mapToDocumentResponse(document, role);
        });
    }
    
    /**
     * Get recently updated documents
     */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getRecentDocuments(UUID userId, int limit) {
        log.debug("Fetching recent documents for user {}", userId);
        
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(7);
        List<Document> documents = documentRepository.findRecentlyUpdated(userId, sinceDate);
        
        return documents.stream()
                .limit(limit)
                .map(document -> {
                    CollaboratorRole role = getUserRole(document.getId(), userId, document);
                    return mapToDocumentResponse(document, role);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get user's role for a document
     */
    private CollaboratorRole getUserRole(
            UUID documentId, 
            UUID userId, 
            Document document) {
        
        // Check if user is creator
        if (document.getCreatedBy().getId().equals(userId)) {
            return CollaboratorRole.OWNER;
        }
        
        // Check collaborator role
        return collaboratorRepository.findByDocumentAndUser(documentId, userId)
                .map(DocumentCollaborator::getRole)
                .orElseThrow(() -> new AuthorizationException("Document", "access"));
    }
    
    /**
     * Map Document entity to response DTO
     */
    private DocumentResponse mapToDocumentResponse(
            Document document, 
            CollaboratorRole userRole) {
        
        long collaboratorCount = collaboratorRepository.countActiveCollaborators(document.getId());
        
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .description(document.getDescription())
                .content(document.getContent())
                .isArchived(document.getIsArchived())
                .isPublic(document.getIsPublic())
                .currentVersion(document.getCurrentVersion())
                .createdBy(document.getCreatedBy().getId())
                .creatorName(document.getCreatedBy().getUsername())
                .currentUserRole(userRole)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .collaboratorCount(collaboratorCount)
                .build();
    }
}