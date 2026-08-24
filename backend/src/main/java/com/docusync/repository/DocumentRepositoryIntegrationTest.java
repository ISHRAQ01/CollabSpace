package com.docusync.repository;

import com.docusync.config.BaseIntegrationTest;
import com.docusync.entity.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Document Repository Integration Test
 */
@DataJpaTest
@Sql(scripts = "/db/test-data.sql")
class DocumentRepositoryIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    private static final UUID TEST_USER_1 = 
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_USER_2 = 
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TEST_DOCUMENT_1 = 
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    
    @Test
    @DisplayName("Should find documents created by user")
    void shouldFindDocumentsByCreator() {
        // When
        Page<Document> documents = documentRepository.findByCreatedBy(
                TEST_USER_1, PageRequest.of(0, 10));
        
        // Then
        assertThat(documents).isNotEmpty();
        assertThat(documents.getContent())
                .allMatch(doc -> doc.getCreatedBy().getId().equals(TEST_USER_1));
    }
    
    @Test
    @DisplayName("Should find collaborative documents")
    void shouldFindCollaborativeDocuments() {
        // When
        Page<Document> documents = documentRepository.findCollaborativeDocuments(
                TEST_USER_2, PageRequest.of(0, 10));
        
        // Then
        assertThat(documents).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should find all accessible documents")
    void shouldFindAllAccessibleDocuments() {
        // When
        Page<Document> documents = documentRepository.findAllAccessibleDocuments(
                TEST_USER_1, PageRequest.of(0, 10));
        
        // Then
        assertThat(documents).isNotEmpty();
        assertThat(documents.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    @DisplayName("Should search documents")
    void shouldSearchDocuments() {
        // When
        Page<Document> documents = documentRepository.searchDocuments(
                TEST_USER_1, "Test", PageRequest.of(0, 10));
        
        // Then
        assertThat(documents).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should update document content")
    void shouldUpdateDocumentContent() {
        // Given
        String newContent = "Updated content";
        
        // When
        int updated = documentRepository.updateDocumentContent(
                TEST_DOCUMENT_1, newContent);
        
        // Then
        assertThat(updated).isEqualTo(1);
        
        Document document = documentRepository.findById(TEST_DOCUMENT_1).get();
        assertThat(document.getContent()).isEqualTo(newContent);
        assertThat(document.getCurrentVersion()).isEqualTo(2);
    }
}