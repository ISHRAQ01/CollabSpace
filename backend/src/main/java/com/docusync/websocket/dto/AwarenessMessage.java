package com.docusync.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Awareness Message
 * 
 * Tracks user presence, cursor positions, and selections
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwarenessMessage {
    
    private UUID userId;
    private String username;
    private String color;
    private CursorPosition cursor;
    private Selection selection;
    private UserStatus status;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorPosition {
        private int line;
        private int column;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Selection {
        private int startLine;
        private int startColumn;
        private int endLine;
        private int endColumn;
    }
    
    public enum UserStatus {
        ACTIVE, IDLE, TYPING, AWAY
    }
}