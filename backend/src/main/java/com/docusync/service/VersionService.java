package com.docusync.service;

import com.docusync.dto.version.VersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VersionService {
    Page<VersionResponse> getVersionHistory(UUID documentId, UUID userId, Pageable pageable);
    VersionResponse getVersion(UUID documentId, Long versionNumber, UUID userId);
    void restoreVersion(UUID documentId, Long versionNumber, UUID userId);
}