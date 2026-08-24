package com.docusync.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI Query Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryResponse {
    
    private UUID documentId;
    private String answer;
    private Integer tokensUsed;
    private Double processingTimeMs;
    private LocalDateTime createdAt;
    private List<SourceChunk> sources;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceChunk {
        private UUID chunkId;
        private Integer chunkIndex;
        private String previewText;
        private Double similarityScore;
    }
}