package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authentication failures
 */
public class AuthenticationException extends DocusyncException {
    
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}