package com.docusync.repository;

import com.docusync.entity.DocumentCollaborator;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.entity.DocumentCollaborator.DocumentCollaboratorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DocumentCollaborator Repository
 * 
 * Data access layer for managing document access and permissions
 */
@Repository
public interface DocumentCollaboratorRepository 
        extends JpaRepository<DocumentCollaborator, DocumentCollaboratorId> {
    
    /**
     * Find all collaborators for a document
     */
    @Query("""
           SELECT dc FROM DocumentCollaborator dc 
           JOIN FETCH dc.user 
           WHERE dc.document.id = :documentId 
           AND dc.isActive = true
           ORDER BY dc.user.username
           """)
    List<DocumentCollaborator> findActiveCollaborators(
            @Param("documentId") UUID documentId);
    
    /**
     * Find collaborator by document and user
     */
    @Query("""
           SELECT dc FROM DocumentCollaborator dc 
           WHERE dc.document.id = :documentId 
           AND dc.user.id = :userId
           """)
    Optional<DocumentCollaborator> findByDocumentAndUser(
            @Param("documentId") UUID documentId, 
            @Param("userId") UUID userId);
    
    /**
     * Check if user has specific role for document
     */
    @Query("""
           SELECT COUNT(dc) > 0 FROM DocumentCollaborator dc 
           WHERE dc.document.id = :documentId 
           AND dc.user.id = :userId 
           AND dc.role = :role 
           AND dc.isActive = true
           """)
    boolean hasRole(
            @Param("documentId") UUID documentId, 
            @Param("userId") UUID userId, 
            @Param("role") CollaboratorRole role);
    
    /**
     * Update collaborator role
     */
    @Modifying
    @Query("""
           UPDATE DocumentCollaborator dc 
           SET dc.role = :newRole, 
               dc.updatedAt = CURRENT_TIMESTAMP 
           WHERE dc.document.id = :documentId 
           AND dc.user.id = :userId
           """)
    int updateRole(
            @Param("documentId") UUID documentId, 
            @Param("userId") UUID userId, 
            @Param("newRole") CollaboratorRole newRole);
    
    /**
     * Deactivate collaborator access
     */
    @Modifying
    @Query("""
           UPDATE DocumentCollaborator dc 
           SET dc.isActive = false, 
               dc.updatedAt = CURRENT_TIMESTAMP 
           WHERE dc.document.id = :documentId 
           AND dc.user.id = :userId
           """)
    int deactivateCollaborator(
            @Param("documentId") UUID documentId, 
            @Param("userId") UUID userId);
    
    /**
     * Count active collaborators
     */
    @Query("""
           SELECT COUNT(dc) FROM DocumentCollaborator dc 
           WHERE dc.document.id = :documentId 
           AND dc.isActive = true
           """)
    long countActiveCollaborators(@Param("documentId") UUID documentId);
}