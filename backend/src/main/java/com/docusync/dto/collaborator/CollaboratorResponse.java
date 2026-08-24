package com.docusync.dto.collaborator;

import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Collaborator Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorResponse {
    
    private UUID userId;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private CollaboratorRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}