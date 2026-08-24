package com.docusync.config;

import com.docusync.websocket.RealtimeSessionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration
 * 
 * Enables WebSocket support and registers handlers for real-time collaboration
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final RealtimeSessionHandler realtimeSessionHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeSessionHandler, "/sync/{documentId}")
                .setAllowedOrigins("*")
                .addInterceptors(new WebSocketAuthInterceptor());
    }
}