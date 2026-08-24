package com.docusync.websocket;

import com.docusync.websocket.dto.SyncMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

/**
 * Real-time Session Handler
 * 
 * Handles WebSocket connections for real-time document collaboration
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeSessionHandler extends TextWebSocketHandler {
    
    private final SessionManager sessionManager;
    private final CollaborativeSyncService syncService;
    private final ObjectMapper objectMapper;
    
    /**
     * Handle new WebSocket connection
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String documentId = extractDocumentId(session);
        UUID userId = extractUserId(session);
        
        log.info("WebSocket connection established: session={}, document={}, user={}", 
                session.getId(), documentId, userId);
        
        // Add session to manager
        sessionManager.addSession(documentId, session, userId);
        
        // Notify other users
        SyncMessage joinMessage = SyncMessage.builder()
                .type(SyncMessage.MessageType.JOIN)
                .userId(userId)
                .documentId(documentId)
                .clientId(session.getId())
                .build();
        
        syncService.processSyncMessage(documentId, joinMessage);
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) 
            throws Exception {
        
        String documentId = sessionManager.getDocumentIdForSession(session);
        
        if (documentId == null) {
            log.warn("No document associated with session: {}", session.getId());
            return;
        }
        
        try {
            SyncMessage syncMessage = objectMapper.readValue(
                    message.getPayload(), 
                    SyncMessage.class);
            
            syncMessage.setClientId(session.getId());
            syncMessage.setDocumentId(documentId);
            
            syncService.processSyncMessage(documentId, syncMessage);
            
        } catch (Exception e) {
            log.error("Failed to process WebSocket message: {}", e.getMessage());
            
            // Send error message back to client
            SyncMessage errorMessage = SyncMessage.builder()
                    .type(SyncMessage.MessageType.ERROR)
                    .documentId(documentId)
                    .data(("Failed to process message: " + e.getMessage()).getBytes())
                    .build();
            
            synchronized (session) {
                session.sendMessage(new TextMessage(
                        objectMapper.writeValueAsString(errorMessage)));
            }
        }
    }
    
    /**
     * Handle WebSocket connection closure
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) 
            throws Exception {
        
        String documentId = sessionManager.getDocumentIdForSession(session);
        UUID userId = sessionManager.getUserIdForSession(session);
        
        log.info("WebSocket connection closed: session={}, document={}, status={}", 
                session.getId(), documentId, status);
        
        // Notify other users
        if (documentId != null) {
            SyncMessage leaveMessage = SyncMessage.builder()
                    .type(SyncMessage.MessageType.LEAVE)
                    .userId(userId)
                    .documentId(documentId)
                    .clientId(session.getId())
                    .build();
            
            syncService.processSyncMessage(documentId, leaveMessage);
        }
        
        // Remove session from manager
        sessionManager.removeSession(session);
    }
    
    /**
     * Handle WebSocket errors
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) 
            throws Exception {
        
        log.error("WebSocket transport error for session {}: {}", 
                session.getId(), exception.getMessage());
        
        // Close session on error
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    
    /**
     * Extract document ID from session URI
     */
    private String extractDocumentId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    /**
     * Extract user ID from session attributes
     */
    private UUID extractUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof UUID) {
            return (UUID) userId;
        }
        // For now, generate temporary ID
        // In production, extract from JWT token
        return UUID.randomUUID();
    }
}