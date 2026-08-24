package com.docusync.controller;

import com.docusync.dto.document.DocumentCreateRequest;
import com.docusync.dto.document.DocumentResponse;
import com.docusync.dto.document.DocumentUpdateRequest;
import com.docusync.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final HttpServletRequest httpServletRequest;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentCreateRequest request) {
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.createDocument(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID documentId) {
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.getDocument(documentId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody DocumentUpdateRequest request) {
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.updateDocument(documentId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{documentId}/content")
    public ResponseEntity<DocumentResponse> updateDocumentContent(
            @PathVariable UUID documentId,
            @RequestBody String content) {
        UUID userId = getCurrentUserId();
        DocumentResponse response = documentService.updateDocumentContent(documentId, content, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId) {
        UUID userId = getCurrentUserId();
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getUserDocuments(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<DocumentResponse> documents = documentService.getUserDocuments(userId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<DocumentResponse> documents = documentService.searchUserDocuments(userId, query, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<DocumentResponse>> getRecentDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        UUID userId = getCurrentUserId();
        List<DocumentResponse> documents = documentService.getRecentDocuments(userId, limit);
        return ResponseEntity.ok(documents);
    }

    private UUID getCurrentUserId() {
        Object userId = httpServletRequest.getAttribute("userId");
        if (userId instanceof UUID) {
            return (UUID) userId;
        }
        throw new IllegalStateException("User ID not found in request");
    }
}
