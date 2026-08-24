package com.docusync.websocket;

import com.docusync.websocket.dto.SyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Distributed Pub/Sub Service
 * 
 * Handles cross-node communication using Redis Pub/Sub
 * Enables horizontal scaling of WebSocket connections
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedPubSubService implements MessageListener {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final CollaborativeSyncService syncService;
    private final SessionManager sessionManager;
    
    private static final String DOCUMENT_UPDATES_CHANNEL = "doc:updates";
    private static final String USER_PRESENCE_CHANNEL = "user:presence";
    
    /**
     * Initialize Redis message listener
     */
    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(
                this, 
                new ChannelTopic(DOCUMENT_UPDATES_CHANNEL));
        
        listenerContainer.addMessageListener(
                this, 
                new ChannelTopic(USER_PRESENCE_CHANNEL));
        
        log.info("Redis Pub/Sub listener initialized for channels: {}, {}", 
                DOCUMENT_UPDATES_CHANNEL, USER_PRESENCE_CHANNEL);
    }
    
    /**
     * Handle messages from Redis
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            
            log.debug("Received Redis message on channel {}: {}", channel, body);
            
            switch (channel) {
                case DOCUMENT_UPDATES_CHANNEL -> handleDocumentUpdate(body);
                case USER_PRESENCE_CHANNEL -> handleUserPresence(body);
            }
        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle document update from another node
     */
    private void handleDocumentUpdate(String body) {
        // Parse and broadcast to local sessions
        // In production, deserialize properly
        log.debug("Document update received from another node");
        
        // Forward to local sessions if document is active locally
        // Implementation depends on message format
    }
    
    /**
     * Handle user presence update
     */
    private void handleUserPresence(String body) {
        log.debug("User presence update received");
    }
    
    /**
     * Publish message to all nodes
     */
    public void publish(String channel, Object message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published message to channel {}", channel);
        } catch (Exception e) {
            log.error("Failed to publish to Redis: {}", e.getMessage());
        }
    }
}