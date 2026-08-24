package com.docusync.controller;

import com.docusync.dto.collaborator.CollaboratorAddRequest;
import com.docusync.dto.collaborator.CollaboratorResponse;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.service.CollaboratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Collaborator Controller
 * 
 * REST endpoints for managing document collaborators
 */
@Slf4j
@RestController
@RequestMapping("/documents/{documentId}/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {
    
    private final CollaboratorService collaboratorService;
    
    /**
     * Get all collaborators for a document
     */
    @GetMapping
    public ResponseEntity<List<CollaboratorResponse>> getCollaborators(
            @PathVariable UUID documentId) {
        
        UUID userId = getCurrentUserId();
        List<CollaboratorResponse> collaborators = 
                collaboratorService.getCollaborators(documentId, userId);
        return ResponseEntity.ok(collaborators);
    }
    
    /**
     * Add collaborator to document
     */
    @PostMapping
    public ResponseEntity<CollaboratorResponse> addCollaborator(
            @PathVariable UUID documentId,
            @Valid @RequestBody CollaboratorAddRequest request) {
        
        UUID userId = getCurrentUserId();
        CollaboratorResponse response = 
                collaboratorService.addCollaborator(documentId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update collaborator role
     */
    @PutMapping("/{collaboratorId}")
    public ResponseEntity<CollaboratorResponse> updateCollaboratorRole(
            @PathVariable UUID documentId,
            @PathVariable UUID collaboratorId,
            @RequestParam CollaboratorRole role) {
        
        UUID userId = getCurrentUserId();
        CollaboratorResponse response = 
                collaboratorService.updateCollaboratorRole(
                        documentId, collaboratorId, role, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Remove collaborator from document
     */
    @DeleteMapping("/{collaboratorId}")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable UUID documentId,
            @PathVariable UUID collaboratorId) {
        
        UUID userId = getCurrentUserId();
        collaboratorService.removeCollaborator(documentId, collaboratorId, userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        // Placeholder - will be replaced with proper implementation
        return UUID.randomUUID();
    }
}