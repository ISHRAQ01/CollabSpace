package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authorization failures
 */
public class AuthorizationException extends DocusyncException {
    
    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "AUTHORIZATION_FAILED");
    }
    
    public AuthorizationException(String resource, String action) {
        super(
            String.format("You don't have permission to %s this %s", action, resource),
            HttpStatus.FORBIDDEN,
            "INSUFFICIENT_PERMISSIONS",
            new PermissionDenied(resource, action)
        );
    }
    
    private record PermissionDenied(String resource, String action) {}
}