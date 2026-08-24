package com.docusync.websocket;

import com.docusync.websocket.dto.SyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collaborative Sync Service
 * 
 * Handles CRDT state synchronization between clients
 * Uses Redis for distributed state management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeSyncService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionManager sessionManager;
    
    // In-memory document states for fast access
    private final Map<String, DocumentState> documentStates = new ConcurrentHashMap<>();
    
    private static final String DOCUMENT_STATE_PREFIX = "doc:state:";
    private static final String DOCUMENT_UPDATES_CHANNEL = "doc:updates";
    private static final Duration STATE_EXPIRY = Duration.ofHours(24);
    
    /**
     * Process incoming sync message
     */
    public void processSyncMessage(String documentId, SyncMessage message) {
        log.debug("Processing sync message for document {}: {}", 
                documentId, message.getType());
        
        switch (message.getType()) {
            case SYNC_STEP_1 -> handleInitialSync(documentId, message);
            case SYNC_STEP_2 -> handleSyncResponse(documentId, message);
            case INCREMENTAL_SYNC -> handleIncrementalSync(documentId, message);
            case AWARENESS -> handleAwareness(documentId, message);
            case JOIN -> handleUserJoin(documentId, message);
            case LEAVE -> handleUserLeave(documentId, message);
            case ERROR -> handleError(documentId, message);
        }
    }
    
    /**
     * Handle initial synchronization request
     */
    private void handleInitialSync(String documentId, SyncMessage message) {
        log.debug("Initial sync request for document: {}", documentId);
        
        DocumentState state = getDocumentState(documentId);
        if (state != null) {
            // Send current state to requesting client
            SyncMessage response = SyncMessage.builder()
                    .type(SyncMessage.MessageType.SYNC_STEP_2)
                    .documentId(documentId)
                    .data(state.getStateVector())
                    .build();
            
            // Send response directly to requesting session
            broadcastToDocument(documentId, response);
        }
    }
    
    /**
     * Handle sync response
     */
    private void handleSyncResponse(String documentId, SyncMessage message) {
        log.debug("Sync response for document: {}", documentId);
        updateDocumentState(documentId, message.getData());
    }
    
    /**
     * Handle incremental updates (real-time changes)
     */
    private void handleIncrementalSync(String documentId, SyncMessage message) {
        log.debug("Incremental sync for document: {}", documentId);
        
        // Update local state
        updateDocumentState(documentId, message.getData());
        
        // Broadcast to all other clients
        broadcastToDocument(documentId, message);
        
        // Publish to Redis for distributed nodes
        publishUpdate(documentId, message);
    }
    
    /**
     * Handle awareness updates (cursor positions)
     */
    private void handleAwareness(String documentId, SyncMessage message) {
        log.trace("Awareness update for document: {}", documentId);
        
        // Broadcast awareness to all clients except sender
        broadcastToDocumentExcluding(documentId, message, message.getClientId());
    }
    
    /**
     * Handle user joining document
     */
    private void handleUserJoin(String documentId, SyncMessage message) {
        log.info("User {} joined document {}", message.getUsername(), documentId);
        
        // Notify other users
        broadcastToDocumentExcluding(documentId, message, message.getClientId());
    }
    
    /**
     * Handle user leaving document
     */
    private void handleUserLeave(String documentId, SyncMessage message) {
        log.info("User {} left document {}", message.getUsername(), documentId);
        
        // Notify other users
        broadcastToDocumentExcluding(documentId, message, message.getClientId());
    }
    
    /**
     * Handle error messages
     */
    private void handleError(String documentId, SyncMessage message) {
        log.error("Error in document {}: {}", documentId, new String(message.getData()));
    }
    
    /**
     * Get document state from memory or Redis
     */
    private DocumentState getDocumentState(String documentId) {
        DocumentState state = documentStates.get(documentId);
        
        if (state == null) {
            // Try to load from Redis
            Object redisState = redisTemplate.opsForValue()
                    .get(DOCUMENT_STATE_PREFIX + documentId);
            
            if (redisState instanceof DocumentState) {
                state = (DocumentState) redisState;
                documentStates.put(documentId, state);
            }
        }
        
        return state;
    }
    
    /**
     * Update document state
     */
    private void updateDocumentState(String documentId, byte[] data) {
        DocumentState state = documentStates.computeIfAbsent(
                documentId, 
                k -> new DocumentState());
        
        state.updateStateVector(data);
        
        // Persist to Redis asynchronously
        redisTemplate.opsForValue().set(
                DOCUMENT_STATE_PREFIX + documentId, 
                state, 
                STATE_EXPIRY);
    }
    
    /**
     * Publish update to Redis for distributed nodes
     */
    private void publishUpdate(String documentId, SyncMessage message) {
        try {
            redisTemplate.convertAndSend(
                    DOCUMENT_UPDATES_CHANNEL, 
                    Map.of(
                            "documentId", documentId,
                            "message", message
                    ));
        } catch (Exception e) {
            log.error("Failed to publish update to Redis: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast message to all sessions in document
     */
    private void broadcastToDocument(String documentId, SyncMessage message) {
        sessionManager.getDocumentSessions(documentId).forEach(session -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new org.springframework.web.socket.TextMessage(
                                serializeMessage(message)));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", 
                        session.getId(), e.getMessage());
            }
        });
    }
    
    /**
     * Broadcast to all sessions except sender
     */
    private void broadcastToDocumentExcluding(
            String documentId, 
            SyncMessage message, 
            String excludeClientId) {
        
        sessionManager.getDocumentSessions(documentId).forEach(session -> {
            try {
                if (session.isOpen() && !session.getId().equals(excludeClientId)) {
                    synchronized (session) {
                        session.sendMessage(new org.springframework.web.socket.TextMessage(
                                serializeMessage(message)));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", 
                        session.getId(), e.getMessage());
            }
        });
    }
    
    /**
     * Serialize message to JSON
     */
    private String serializeMessage(SyncMessage message) {
        // For now, simple JSON serialization
        // In production, use Jackson or Protocol Buffers for efficiency
        return String.format(
                "{\"type\":\"%s\",\"userId\":\"%s\",\"username\":\"%s\",\"documentId\":\"%s\",\"timestamp\":%d}",
                message.getType(),
                message.getUserId(),
                message.getUsername(),
                message.getDocumentId(),
                message.getTimestamp()
        );
    }
    
    /**
     * Document State Holder
     */
    private static class DocumentState {
        private byte[] stateVector;
        private long lastUpdate;
        
        public void updateStateVector(byte[] newState) {
            // In production, this would merge CRDT state vectors
            this.stateVector = newState;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public byte[] getStateVector() {
            return stateVector;
        }
    }
}