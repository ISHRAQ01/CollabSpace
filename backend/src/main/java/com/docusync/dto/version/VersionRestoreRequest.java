package com.docusync.dto.version;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Version Restore Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionRestoreRequest {
    
    @NotNull(message = "Version number is required")
    private Long versionNumber;
    
    @Builder.Default
    private String restoreNote = "Restored from version history";
}