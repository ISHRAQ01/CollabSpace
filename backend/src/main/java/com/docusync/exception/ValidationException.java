package com.docusync.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown for validation failures
 */
public class ValidationException extends DocusyncException {
    
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED");
    }
    
    public ValidationException(String message, Map<String, String> errors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", errors);
    }
}