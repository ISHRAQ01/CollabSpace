package com.docusync.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base Exception for DocuSync
 * 
 * All custom exceptions should extend this class
 */
@Getter
public class DocusyncException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;
    private final Object details;
    
    public DocusyncException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public DocusyncException(String message, HttpStatus status, String errorCode, Object details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public DocusyncException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.details = null;
    }
}