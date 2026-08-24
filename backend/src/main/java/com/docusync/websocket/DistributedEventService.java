package com.docusync.websocket;

import com.docusync.websocket.SessionManager;
import com.docusync.websocket.dto.SyncMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Event Service
 * 
 * Handles cross-node events using Redis Pub/Sub
 * Enables horizontal scaling of WebSocket connections
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedEventService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;
    
    // Channel prefixes
    private static final String DOCUMENT_UPDATES_CHANNEL = "doc:updates:";
    private static final String USER_PRESENCE_CHANNEL = "user:presence:";
    private static final String DOCUMENT_LOCKS_CHANNEL = "doc:locks:";
    
    // Cache keys
    private static final String ACTIVE_DOCUMENTS_KEY = "active:documents";
    private static final String DOCUMENT_USERS_PREFIX = "doc:users:";
    
    /**
     * Handle document update from another node
     */
    public void handleDocumentUpdate(String message) {
        try {
            log.debug("Received document update from another node");
            
            // Parse message
            Map<String, Object> eventData = objectMapper.readValue(
                    message, Map.class);
            
            String documentId = (String) eventData.get("documentId");
            String payload = (String) eventData.get("payload");
            
            // Broadcast to local sessions
            SyncMessage syncMessage = objectMapper.readValue(
                    payload, SyncMessage.class);
            
            broadcastToLocalSessions(documentId, syncMessage);
            
        } catch (Exception e) {
            log.error("Failed to handle document update: {}", e.getMessage());
        }
    }
    
    /**
     * Handle user presence update from another node
     */
    public void handleUserPresence(String message) {
        try {
            log.debug("Received user presence update");
            
            Map<String, Object> presenceData = objectMapper.readValue(
                    message, Map.class);
            
            String documentId = (String) presenceData.get("documentId");
            String userId = (String) presenceData.get("userId");
            String action = (String) presenceData.get("action");
            
            log.debug("User {} {} document {}", userId, action, documentId);
            
            // Update local cache
            if ("joined".equals(action)) {
                addUserToDocument(documentId, userId);
            } else if ("left".equals(action)) {
                removeUserFromDocument(documentId, userId);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle presence update: {}", e.getMessage());
        }
    }
    
    /**
     * Publish document update to all nodes
     */
    public void publishDocumentUpdate(String documentId, SyncMessage message) {
        try {
            String channel = DOCUMENT_UPDATES_CHANNEL + documentId;
            String payload = objectMapper.writeValueAsString(message);
            
            Map<String, Object> eventData = Map.of(
                    "documentId", documentId,
                    "payload", payload,
                    "timestamp", System.currentTimeMillis()
            );
            
            redisTemplate.convertAndSend(channel, eventData);
            log.trace("Published update to channel {}", channel);
            
        } catch (Exception e) {
            log.error("Failed to publish document update: {}", e.getMessage());
        }
    }
    
    /**
     * Publish user presence to all nodes
     */
    public void publishUserPresence(
            String documentId, 
            UUID userId, 
            String action) {
        
        try {
            String channel = USER_PRESENCE_CHANNEL + documentId;
            
            Map<String, Object> presenceData = Map.of(
                    "documentId", documentId,
                    "userId", userId.toString(),
                    "action", action,
                    "timestamp", System.currentTimeMillis()
            );
            
            redisTemplate.convertAndSend(channel, presenceData);
            log.trace("Published presence to channel {}", channel);
            
        } catch (Exception e) {
            log.error("Failed to publish presence: {}", e.getMessage());
        }
    }
    
    /**
     * Broadcast message to local sessions only
     */
    private void broadcastToLocalSessions(String documentId, SyncMessage message) {
        sessionManager.getDocumentSessions(documentId).forEach(session -> {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(
                                objectMapper.writeValueAsString(message)));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to broadcast to session {}: {}", 
                        session.getId(), e.getMessage());
            }
        });
    }
    
    /**
     * Add user to document's active users
     */
    private void addUserToDocument(String documentId, String userId) {
        String key = DOCUMENT_USERS_PREFIX + documentId;
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    /**
     * Remove user from document's active users
     */
    private void removeUserFromDocument(String documentId, String userId) {
        String key = DOCUMENT_USERS_PREFIX + documentId;
        redisTemplate.opsForSet().remove(key, userId);
    }
    
    /**
     * Get active users for a document
     */
    public java.util.Set<String> getActiveUsers(String documentId) {
        String key = DOCUMENT_USERS_PREFIX + documentId;
        return (java.util.Set<String>) (java.util.Set<?>) redisTemplate.opsForSet().members(key);
    }
    
    /**
     * Track active document in Redis
     */
    public void trackActiveDocument(String documentId) {
        redisTemplate.opsForSet().add(ACTIVE_DOCUMENTS_KEY, documentId);
        redisTemplate.expire(ACTIVE_DOCUMENTS_KEY, 24, TimeUnit.HOURS);
    }
    
    /**
     * Remove document from active tracking
     */
    public void untrackActiveDocument(String documentId) {
        redisTemplate.opsForSet().remove(ACTIVE_DOCUMENTS_KEY, documentId);
    }
}
