package com.docusync.repository;

import com.docusync.entity.VersionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * VersionHistory Repository
 * 
 * Data access layer for document version management
 */
@Repository
public interface VersionHistoryRepository 
        extends JpaRepository<VersionHistory, UUID> {
    
    /**
     * Find all versions for a document (paginated)
     */
    @Query("""
           SELECT vh FROM VersionHistory vh 
           JOIN FETCH vh.createdBy 
           WHERE vh.document.id = :documentId 
           ORDER BY vh.versionNumber DESC
           """)
    Page<VersionHistory> findByDocumentId(
            @Param("documentId") UUID documentId, 
            Pageable pageable);
    
    /**
     * Find specific version of a document
     */
    @Query("""
           SELECT vh FROM VersionHistory vh 
           WHERE vh.document.id = :documentId 
           AND vh.versionNumber = :versionNumber
           """)
    Optional<VersionHistory> findByDocumentAndVersion(
            @Param("documentId") UUID documentId, 
            @Param("versionNumber") Long versionNumber);
    
    /**
     * Get latest version number for a document
     */
    @Query("""
           SELECT MAX(vh.versionNumber) FROM VersionHistory vh 
           WHERE vh.document.id = :documentId
           """)
    Long findLatestVersionNumber(@Param("documentId") UUID documentId);
    
    /**
     * Find versions created after a specific date
     */
    @Query("""
           SELECT vh FROM VersionHistory vh 
           WHERE vh.document.id = :documentId 
           AND vh.createdAt > :sinceDate
           ORDER BY vh.createdAt DESC
           """)
    List<VersionHistory> findRecentVersions(
            @Param("documentId") UUID documentId, 
            @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Count total versions for a document
     */
    @Query("""
           SELECT COUNT(vh) FROM VersionHistory vh 
           WHERE vh.document.id = :documentId
           """)
    long countVersions(@Param("documentId") UUID documentId);
    
    /**
     * Delete old versions (keep latest N versions)
     */
    @Query("""
           DELETE FROM VersionHistory vh 
           WHERE vh.document.id = :documentId 
           AND vh.versionNumber <= (
               SELECT MAX(vh2.versionNumber) - :keepCount 
               FROM VersionHistory vh2 
               WHERE vh2.document.id = :documentId
           )
           """)
    void deleteOldVersions(
            @Param("documentId") UUID documentId, 
            @Param("keepCount") Long keepCount);
}