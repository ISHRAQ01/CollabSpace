package com.docusync.service;

import com.docusync.config.BaseIntegrationTest;
import com.docusync.dto.document.DocumentCreateRequest;
import com.docusync.dto.document.DocumentResponse;
import com.docusync.dto.document.DocumentUpdateRequest;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.exception.AuthorizationException;
import com.docusync.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Document Service Integration Test
 */
@Sql(scripts = "/db/test-data.sql")
class DocumentServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private DocumentService documentService;
    
    private static final UUID TEST_USER_1 = 
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_2 = 
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TEST_DOCUMENT_1 = 
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    
    @Test
    @DisplayName("Should create document successfully")
    void shouldCreateDocument() {
        // Given
        DocumentCreateRequest request = DocumentCreateRequest.builder()
                .title("New Test Document")
                .description("Created in test")
                .isPublic(false)
                .build();
        
        // When
        DocumentResponse response = documentService.createDocument(
                request, TEST_USER_1);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Test Document");
        assertThat(response.getCurrentUserRole()).isEqualTo(CollaboratorRole.OWNER);
    }
    
    @Test
    @DisplayName("Should get document with proper permissions")
    void shouldGetDocument() {
        // When
        DocumentResponse response = documentService.getDocument(
                TEST_DOCUMENT_1, TEST_USER_1);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_DOCUMENT_1);
        assertThat(response.getCurrentUserRole()).isEqualTo(CollaboratorRole.OWNER);
    }
    
    @Test
    @DisplayName("Should throw exception for non-existent document")
    void shouldThrowExceptionForNonExistentDocument() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> documentService.getDocument(
                nonExistentId, TEST_USER_1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    @DisplayName("Should update document metadata")
    void shouldUpdateDocument() {
        // Given
        DocumentUpdateRequest request = DocumentUpdateRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .build();
        
        // When
        DocumentResponse response = documentService.updateDocument(
                TEST_DOCUMENT_1, request, TEST_USER_1);
        
        // Then
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }
    
    @Test
    @DisplayName("Should throw exception for unauthorized access")
    void shouldThrowExceptionForUnauthorizedAccess() {
        // Given
        UUID unauthorizedUser = UUID.randomUUID();
        
        // When/Then
        assertThatThrownBy(() -> documentService.getDocument(
                TEST_DOCUMENT_1, unauthorizedUser))
                .isInstanceOf(AuthorizationException.class);
    }
    
    @Test
    @DisplayName("Should get user documents with pagination")
    void shouldGetUserDocuments() {
        // When
        var documents = documentService.getUserDocuments(
                TEST_USER_1, PageRequest.of(0, 10));
        
        // Then
        assertThat(documents).isNotNull();
        assertThat(documents.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}