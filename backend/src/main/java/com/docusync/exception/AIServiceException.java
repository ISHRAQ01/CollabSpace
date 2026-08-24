package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for AI service errors
 */
public class AIServiceException extends DocusyncException {
    
    public AIServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_ERROR");
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_ERROR");
    }
    
    public AIServiceException(String operation, String reason) {
        super(
            String.format("AI operation '%s' failed: %s", operation, reason),
            HttpStatus.SERVICE_UNAVAILABLE,
            "AI_SERVICE_ERROR",
            new AIServiceError(operation, reason)
        );
    }
    
    private record AIServiceError(String operation, String reason) {}
}