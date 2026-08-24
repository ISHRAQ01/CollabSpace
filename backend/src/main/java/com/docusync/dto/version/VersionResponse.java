package com.docusync.dto.version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Version History Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionResponse {
    
    private UUID id;
    private Long versionNumber;
    private String changeSummary;
    private Long changeSize;
    private UUID createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
}