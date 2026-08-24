package com.docusync.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI Query Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryRequest {
    
    private UUID documentId;
    
    @NotBlank(message = "Prompt is required")
    @Size(min = 3, max = 2000, message = "Prompt must be between 3 and 2000 characters")
    private String prompt;
    
    @Builder.Default
    private Integer maxTokens = 500;
    
    @Builder.Default
    private Double temperature = 0.7;
    
    @Builder.Default
    private Boolean useContext = true;
}