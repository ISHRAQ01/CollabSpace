package com.docusync.dto.document;

import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Document Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String content;
    private Boolean isArchived;
    private Boolean isPublic;
    private Long currentVersion;
    private UUID createdBy;
    private String creatorName;
    private CollaboratorRole currentUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long collaboratorCount;
}