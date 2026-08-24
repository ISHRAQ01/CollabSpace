package com.docusync.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Exception Handler
 * 
 * Handles exceptions thrown during WebSocket message processing
 */
@Slf4j
@Controller
public class WebSocketExceptionHandler {
    
    /**
     * Handle WebSocket exceptions and send error to user
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        log.error("WebSocket error occurred", ex);
        
        if (ex instanceof DocusyncException docusyncException) {
            return String.format(
                "{\"errorCode\":\"%s\",\"message\":\"%s\"}",
                docusyncException.getErrorCode(),
                docusyncException.getMessage()
            );
        }
        
        return "{\"errorCode\":\"WEBSOCKET_ERROR\",\"message\":\"An error occurred during real-time communication\"}";
    }
}