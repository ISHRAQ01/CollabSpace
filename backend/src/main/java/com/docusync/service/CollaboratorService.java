package com.docusync.service;

import com.docusync.dto.collaborator.CollaboratorAddRequest;
import com.docusync.dto.collaborator.CollaboratorResponse;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;

import java.util.List;
import java.util.UUID;

public interface CollaboratorService {
    List<CollaboratorResponse> getCollaborators(UUID documentId, UUID userId);
    CollaboratorResponse addCollaborator(UUID documentId, CollaboratorAddRequest request, UUID userId);
    CollaboratorResponse updateCollaboratorRole(UUID documentId, UUID collaboratorId, CollaboratorRole role, UUID userId);
    void removeCollaborator(UUID documentId, UUID collaboratorId, UUID userId);
}