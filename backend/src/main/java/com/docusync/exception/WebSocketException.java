package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for WebSocket-related errors
 */
public class WebSocketException extends DocusyncException {
    
    public WebSocketException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "WEBSOCKET_ERROR");
    }
    
    public WebSocketException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "WEBSOCKET_ERROR");
    }
    
    public WebSocketException(String documentId, String reason) {
        super(
            String.format("WebSocket error for document %s: %s", documentId, reason),
            HttpStatus.INTERNAL_SERVER_ERROR,
            "WEBSOCKET_ERROR",
            new WebSocketErrorDetails(documentId, reason)
        );
    }
    
    private record WebSocketErrorDetails(String documentId, String reason) {}
}