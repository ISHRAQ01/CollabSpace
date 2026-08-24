package com.docusync.controller;

import com.docusync.dto.version.VersionResponse;
import com.docusync.dto.version.VersionRestoreRequest;
import com.docusync.service.VersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Version Controller
 * 
 * REST endpoints for document version management
 */
@Slf4j
@RestController
@RequestMapping("/documents/{documentId}/versions")
@RequiredArgsConstructor
public class VersionController {
    
    private final VersionService versionService;
    
    /**
     * Get version history for a document
     */
    @GetMapping
    public ResponseEntity<Page<VersionResponse>> getVersionHistory(
            @PathVariable UUID documentId,
            Pageable pageable) {
        
        UUID userId = getCurrentUserId();
        Page<VersionResponse> versions = 
                versionService.getVersionHistory(documentId, userId, pageable);
        return ResponseEntity.ok(versions);
    }
    
    /**
     * Get specific version
     */
    @GetMapping("/{versionNumber}")
    public ResponseEntity<VersionResponse> getVersion(
            @PathVariable UUID documentId,
            @PathVariable Long versionNumber) {
        
        UUID userId = getCurrentUserId();
        VersionResponse version = 
                versionService.getVersion(documentId, versionNumber, userId);
        return ResponseEntity.ok(version);
    }
    
    /**
     * Restore document to specific version
     */
    @PostMapping("/restore")
    public ResponseEntity<Void> restoreVersion(
            @PathVariable UUID documentId,
            @Valid @RequestBody VersionRestoreRequest request) {
        
        UUID userId = getCurrentUserId();
        versionService.restoreVersion(
                documentId, request.getVersionNumber(), userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get current authenticated user ID
     */
    private UUID getCurrentUserId() {
        // Placeholder - will be replaced with proper implementation
        return UUID.randomUUID();
    }
}