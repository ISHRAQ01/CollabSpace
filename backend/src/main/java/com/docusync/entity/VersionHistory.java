package com.docusync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * VersionHistory Entity
 * 
 * Tracks document versions for history and restore functionality
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "version_history",
    indexes = {
        @Index(name = "idx_version_document_id", columnList = "document_id"),
        @Index(name = "idx_version_created_at", columnList = "created_at")
    }
)
public class VersionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(name = "version_number", nullable = false)
    private Long versionNumber;
    
    @Column(name = "content_snapshot", columnDefinition = "TEXT", nullable = false)
    private String contentSnapshot;
    
    @Column(name = "change_summary", length = 500)
    private String changeSummary;
    
    @Column(name = "change_size", nullable = false)
    private Long changeSize;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}