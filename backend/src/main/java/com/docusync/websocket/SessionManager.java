package com.docusync.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket Session Manager
 * 
 * Manages active WebSocket sessions for real-time collaboration
 * Thread-safe implementation using concurrent collections
 */
@Slf4j
@Component
public class SessionManager {
    
    // documentId -> Set of active sessions
    private final Map<String, Set<WebSocketSession>> documentSessions = new ConcurrentHashMap<>();
    
    // sessionId -> documentId mapping for quick lookup
    private final Map<String, String> sessionDocumentMap = new ConcurrentHashMap<>();
    
    // sessionId -> userId mapping for user tracking
    private final Map<String, UUID> sessionUserMap = new ConcurrentHashMap<>();
    
    /**
     * Add session to a document
     */
    public void addSession(String documentId, WebSocketSession session, UUID userId) {
        String sessionId = session.getId();
        
        documentSessions.computeIfAbsent(documentId, k -> new CopyOnWriteArraySet<>())
                .add(session);
        sessionDocumentMap.put(sessionId, documentId);
        sessionUserMap.put(sessionId, userId);
        
        log.info("Session {} added to document {}. Active sessions: {}", 
                sessionId, documentId, getActiveSessionCount(documentId));
    }
    
    /**
     * Remove session from a document
     */
    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        String documentId = sessionDocumentMap.remove(sessionId);
        sessionUserMap.remove(sessionId);
        
        if (documentId != null) {
            Set<WebSocketSession> sessions = documentSessions.get(documentId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    documentSessions.remove(documentId);
                }
            }
            log.info("Session {} removed from document {}. Remaining sessions: {}", 
                    sessionId, documentId, getActiveSessionCount(documentId));
        }
    }
    
    /**
     * Get all active sessions for a document
     */
    public Set<WebSocketSession> getDocumentSessions(String documentId) {
        return documentSessions.getOrDefault(documentId, Set.of());
    }
    
    /**
     * Get active session count for a document
     */
    public int getActiveSessionCount(String documentId) {
        Set<WebSocketSession> sessions = documentSessions.get(documentId);
        return sessions != null ? sessions.size() : 0;
    }
    
    /**
     * Get user ID for a session
     */
    public UUID getUserIdForSession(WebSocketSession session) {
        return sessionUserMap.get(session.getId());
    }
    
    /**
     * Get document ID for a session
     */
    public String getDocumentIdForSession(WebSocketSession session) {
        return sessionDocumentMap.get(session.getId());
    }
    
    /**
     * Check if document has active sessions
     */
    public boolean hasActiveSessions(String documentId) {
        return getActiveSessionCount(documentId) > 0;
    }
    
    /**
     * Get all active document IDs
     */
    public Set<String> getActiveDocuments() {
        return documentSessions.keySet();
    }
    
    /**
     * Get total active sessions across all documents
     */
    public int getTotalActiveSessions() {
        return sessionDocumentMap.size();
    }
    
    /**
     * Clean up all sessions
     */
    public void clearAll() {
        documentSessions.clear();
        sessionDocumentMap.clear();
        sessionUserMap.clear();
        log.info("All WebSocket sessions cleared");
    }
}