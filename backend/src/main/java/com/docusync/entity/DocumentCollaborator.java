package com.docusync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DocumentCollaborator Entity
 * 
 * Represents a user's access to a specific document with role-based permissions
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "document_collaborators",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_document_user",
            columnNames = {"document_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_collab_user_id", columnList = "user_id"),
        @Index(name = "idx_collab_document_id", columnList = "document_id")
    }
)
public class DocumentCollaborator {
    
    @EmbeddedId
    private DocumentCollaboratorId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentId")
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private CollaboratorRole role = CollaboratorRole.VIEWER;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Composite primary key
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentCollaboratorId implements Serializable {
        
        @Column(name = "document_id")
        private UUID documentId;
        
        @Column(name = "user_id")
        private UUID userId;
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DocumentCollaboratorId that = (DocumentCollaboratorId) o;
            return Objects.equals(documentId, that.documentId) && 
                   Objects.equals(userId, that.userId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(documentId, userId);
        }
    }
    
    /**
     * Collaborator roles enum
     */
    public enum CollaboratorRole {
        OWNER, EDITOR, VIEWER, COMMENTATOR
    }
}