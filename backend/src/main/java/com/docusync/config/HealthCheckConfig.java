package com.docusync.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Health Check Configuration
 * 
 * Custom health indicators for monitoring
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {
    
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Database health indicator
     */
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            try {
                Integer result = jdbcTemplate.queryForObject(
                        "SELECT 1", Integer.class);
                
                if (result != null && result == 1) {
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connected")
                            .build();
                }
                
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Query failed")
                        .build();
                        
            } catch (Exception e) {
                log.error("Database health check failed: {}", e.getMessage());
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
    
    /**
     * Redis health indicator
     */
    @Bean
    public HealthIndicator redisHealthIndicator() {
        return () -> {
            try {
                redisTemplate.opsForValue().set("health:check", "ok");
                String result = (String) redisTemplate.opsForValue()
                        .get("health:check");
                
                if ("ok".equals(result)) {
                    return Health.up()
                            .withDetail("redis", "Connected")
                            .build();
                }
                
                return Health.down()
                        .withDetail("redis", "Check failed")
                        .build();
                        
            } catch (Exception e) {
                log.error("Redis health check failed: {}", e.getMessage());
                return Health.down()
                        .withDetail("redis", "Disconnected")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
    
    /**
     * AI service health indicator
     */
    @Bean
    public HealthIndicator aiServiceHealthIndicator() {
        return () -> {
            // Check if AI service is configured and reachable
            // This is a simplified check
            boolean aiConfigured = System.getenv("OPENAI_API_KEY") != null 
                    || System.getenv("OLLAMA_HOST") != null;
            
            if (aiConfigured) {
                return Health.up()
                        .withDetail("ai-service", "Configured")
                        .build();
            }
            
            return Health.unknown()
                    .withDetail("ai-service", "Not configured")
                    .build();
        };
    }
}