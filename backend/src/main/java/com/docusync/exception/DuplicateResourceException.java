package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a duplicate resource
 */
public class DuplicateResourceException extends DocusyncException {
    
    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.CONFLICT,
            "DUPLICATE_RESOURCE",
            new ErrorDetails(resourceName, fieldName, fieldValue)
        );
    }
    
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
    
    private record ErrorDetails(String resourceName, String fieldName, String fieldValue) {}
}