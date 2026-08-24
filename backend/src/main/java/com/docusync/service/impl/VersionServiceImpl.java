package com.docusync.service.impl;

import com.docusync.dto.version.VersionResponse;
import com.docusync.entity.VersionHistory;
import com.docusync.repository.VersionHistoryRepository;
import com.docusync.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
    
    private final VersionHistoryRepository versionHistoryRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<VersionResponse> getVersionHistory(UUID documentId, UUID userId, Pageable pageable) {
        return versionHistoryRepository.findByDocumentId(documentId, pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public VersionResponse getVersion(UUID documentId, Long versionNumber, UUID userId) {
        VersionHistory version = versionHistoryRepository.findByDocumentAndVersion(documentId, versionNumber)
                .orElseThrow(() -> new RuntimeException("Version not found"));
        return mapToResponse(version);
    }
    
    @Override
    @Transactional
    public void restoreVersion(UUID documentId, Long versionNumber, UUID userId) {
        // Implementation for version restore
    }
    
    private VersionResponse mapToResponse(VersionHistory version) {
        return VersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .changeSummary(version.getChangeSummary())
                .changeSize(version.getChangeSize())
                .createdBy(version.getCreatedBy().getId())
                .creatorName(version.getCreatedBy().getUsername())
                .createdAt(version.getCreatedAt())
                .build();
    }
}