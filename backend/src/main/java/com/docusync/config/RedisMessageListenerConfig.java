package com.docusync.config;

import com.docusync.websocket.DistributedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis Message Listener Configuration
 * 
 * Configures Redis Pub/Sub for distributed event handling
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisMessageListenerConfig {
    
    /**
     * Redis message listener container
     * Handles subscriptions to Redis channels
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter documentUpdateListener,
            MessageListenerAdapter userPresenceListener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Subscribe to document update channel
        container.addMessageListener(
                documentUpdateListener, 
                new PatternTopic("doc:updates:*"));
        
        // Subscribe to user presence channel
        container.addMessageListener(
                userPresenceListener, 
                new PatternTopic("user:presence:*"));
        
        log.info("Redis message listener container initialized");
        return container;
    }
    
    /**
     * Document update listener adapter
     */
    @Bean
    public MessageListenerAdapter documentUpdateListener(
            DistributedEventService eventService) {
        
        MessageListenerAdapter adapter = new MessageListenerAdapter(eventService);
        adapter.setDefaultListenerMethod("handleDocumentUpdate");
        return adapter;
    }
    
    /**
     * User presence listener adapter
     */
    @Bean
    public MessageListenerAdapter userPresenceListener(
            DistributedEventService eventService) {
        
        MessageListenerAdapter adapter = new MessageListenerAdapter(eventService);
        adapter.setDefaultListenerMethod("handleUserPresence");
        return adapter;
    }
}
