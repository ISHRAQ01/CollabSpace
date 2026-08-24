package com.docusync.repository;

import com.docusync.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Document Repository
 * 
 * Data access layer for Document entity with complex queries
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    
    /**
     * Find documents created by a user
     */
    @Query("""
           SELECT d FROM Document d 
           WHERE d.createdBy.id = :userId 
           AND d.isArchived = false 
           ORDER BY d.updatedAt DESC
           """)
    Page<Document> findByCreatedBy(
            @Param("userId") UUID userId, 
            Pageable pageable);
    
    /**
     * Find documents where user is a collaborator
     */
    @Query("""
           SELECT DISTINCT d FROM Document d 
           JOIN d.collaborators c 
           WHERE c.user.id = :userId 
           AND c.isActive = true 
           AND d.isArchived = false 
           ORDER BY d.updatedAt DESC
           """)
    Page<Document> findCollaborativeDocuments(
            @Param("userId") UUID userId, 
            Pageable pageable);
    
    /**
     * Find all documents accessible to a user (owned + collaborative)
     */
    @Query("""
           SELECT DISTINCT d FROM Document d 
           LEFT JOIN d.collaborators c 
           WHERE (d.createdBy.id = :userId OR c.user.id = :userId) 
           AND d.isArchived = false 
           AND (c IS NULL OR c.isActive = true)
           ORDER BY d.updatedAt DESC
           """)
    Page<Document> findAllAccessibleDocuments(
            @Param("userId") UUID userId, 
            Pageable pageable);
    
    /**
     * Search documents by title or description
     */
    @Query("""
           SELECT DISTINCT d FROM Document d 
           LEFT JOIN d.collaborators c 
           WHERE (d.createdBy.id = :userId OR c.user.id = :userId) 
           AND d.isArchived = false 
           AND (
               LOWER(d.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
               OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           )
           ORDER BY d.updatedAt DESC
           """)
    Page<Document> searchDocuments(
            @Param("userId") UUID userId, 
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    /**
     * Find public documents
     */
    @Query("""
           SELECT d FROM Document d 
           WHERE d.isPublic = true 
           AND d.isArchived = false 
           ORDER BY d.updatedAt DESC
           """)
    Page<Document> findPublicDocuments(Pageable pageable);
    
    /**
     * Update document content with version increment
     */
    @Modifying
    @Query("""
           UPDATE Document d 
           SET d.content = :content, 
               d.currentVersion = d.currentVersion + 1,
               d.updatedAt = CURRENT_TIMESTAMP 
           WHERE d.id = :documentId
           """)
    int updateDocumentContent(
            @Param("documentId") UUID documentId, 
            @Param("content") String content);
    
    /**
     * Archive document
     */
    @Modifying
    @Query("UPDATE Document d SET d.isArchived = true WHERE d.id = :documentId")
    void archiveDocument(@Param("documentId") UUID documentId);
    
    /**
     * Find recently updated documents for a user
     */
    @Query("""
           SELECT DISTINCT d FROM Document d 
           LEFT JOIN d.collaborators c 
           WHERE (d.createdBy.id = :userId OR c.user.id = :userId) 
           AND d.isArchived = false 
           AND d.updatedAt > :sinceDate
           ORDER BY d.updatedAt DESC
           """)
    List<Document> findRecentlyUpdated(
            @Param("userId") UUID userId, 
            @Param("sinceDate") java.time.LocalDateTime sinceDate);
}