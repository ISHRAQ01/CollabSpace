package com.docusync.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * WebSocket Sync Message
 * 
 * Represents a synchronization message between clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncMessage {
    
    public enum MessageType {
        SYNC_STEP_1,      // Initial sync request
        SYNC_STEP_2,      // Sync response
        INCREMENTAL_SYNC, // Real-time updates
        AWARENESS,        // Cursor presence
        JOIN,             // User joined
        LEAVE,            // User left
        ERROR             // Error message
    }
    
    private MessageType type;
    private UUID userId;
    private String username;
    private String documentId;
    private byte[] data;
    private long timestamp;
    private String clientId;
    
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}