package com.docusync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Document Entity
 * 
 * Represents a collaborative document in the system
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "documents",
    indexes = {
        @Index(name = "idx_documents_created_by", columnList = "created_by"),
        @Index(name = "idx_documents_updated_at", columnList = "updated_at")
    }
)
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;
    
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;
    
    @Column(name = "current_version", nullable = false)
    @Builder.Default
    private Long currentVersion = 1L;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<DocumentCollaborator> collaborators = new HashSet<>();
    
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VersionHistory> versionHistory = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "optimistic_lock_version")
    private Long optimisticLockVersion;
}