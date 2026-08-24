package com.docusync.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Authentication Interceptor
 * 
 * Validates JWT token during WebSocket handshake
 */
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        
        // Extract token from query parameter or header
        String token = extractToken(request);
        
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket handshake failed: No token provided");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        
        // TODO: Validate JWT token and extract user info
        // For now, we'll just store the token in attributes
        attributes.put("token", token);
        
        // Extract document ID from URI
        String path = request.getURI().getPath();
        String documentId = path.substring(path.lastIndexOf('/') + 1);
        attributes.put("documentId", documentId);
        
        log.info("WebSocket handshake successful for document: {}", documentId);
        return true;
    }
    
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // Post-handshake logic (if needed)
    }
    
    private String extractToken(ServerHttpRequest request) {
        // Check query parameter first
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            return query.substring(query.indexOf("token=") + 6);
        }
        
        // Check authorization header
        var headers = request.getHeaders();
        var authHeader = headers.getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
}