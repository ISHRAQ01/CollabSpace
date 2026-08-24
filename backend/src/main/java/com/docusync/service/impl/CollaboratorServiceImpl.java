package com.docusync.service.impl;

import com.docusync.dto.collaborator.CollaboratorAddRequest;
import com.docusync.dto.collaborator.CollaboratorResponse;
import com.docusync.entity.Document;
import com.docusync.entity.DocumentCollaborator;
import com.docusync.entity.User;
import com.docusync.entity.DocumentCollaborator.CollaboratorRole;
import com.docusync.repository.DocumentCollaboratorRepository;
import com.docusync.repository.DocumentRepository;
import com.docusync.repository.UserRepository;
import com.docusync.service.CollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {
    
    private final DocumentCollaboratorRepository collaboratorRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorResponse> getCollaborators(UUID documentId, UUID userId) {
        List<DocumentCollaborator> collaborators = collaboratorRepository.findActiveCollaborators(documentId);
        return collaborators.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CollaboratorResponse addCollaborator(UUID documentId, CollaboratorAddRequest request, UUID userId) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        DocumentCollaborator collaborator = DocumentCollaborator.builder()
                .id(new DocumentCollaborator.DocumentCollaboratorId(documentId, user.getId()))
                .document(document)
                .user(user)
                .role(request.getRole())
                .isActive(true)
                .build();
        
        collaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(collaborator);
    }
    
    @Override
    @Transactional
    public CollaboratorResponse updateCollaboratorRole(UUID documentId, UUID collaboratorId, CollaboratorRole role, UUID userId) {
        collaboratorRepository.updateRole(documentId, collaboratorId, role);
        DocumentCollaborator collaborator = collaboratorRepository.findByDocumentAndUser(documentId, collaboratorId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));
        return mapToResponse(collaborator);
    }
    
    @Override
    @Transactional
    public void removeCollaborator(UUID documentId, UUID collaboratorId, UUID userId) {
        collaboratorRepository.deactivateCollaborator(documentId, collaboratorId);
    }
    
    private CollaboratorResponse mapToResponse(DocumentCollaborator collaborator) {
        User user = collaborator.getUser();
        return CollaboratorResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(collaborator.getRole())
                .isActive(collaborator.getIsActive())
                .createdAt(collaborator.getCreatedAt())
                .updatedAt(collaborator.getUpdatedAt())
                .build();
    }
}