package com.docusync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Service
 * 
 * Implements Redis-based rate limiting for API endpoints
 * Prevents abuse and ensures fair usage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "rate:";
    
    /**
     * Check if request is allowed
     */
    public boolean isAllowed(String key, int maxRequests, long windowSeconds) {
        String rateKey = RATE_LIMIT_PREFIX + key;
        
        Long currentCount = redisTemplate.opsForValue().increment(rateKey);
        
        if (currentCount != null && currentCount == 1) {
            // First request in window, set expiry
            redisTemplate.expire(rateKey, windowSeconds, TimeUnit.SECONDS);
        }
        
        return currentCount != null && currentCount <= maxRequests;
    }
    
    /**
     * Check if request is allowed with custom key
     */
    public boolean isAllowed(String key, int maxRequests, long windowSeconds, TimeUnit timeUnit) {
        String rateKey = RATE_LIMIT_PREFIX + key;
        
        Long currentCount = redisTemplate.opsForValue().increment(rateKey);
        
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(rateKey, windowSeconds, timeUnit);
        }
        
        return currentCount != null && currentCount <= maxRequests;
    }
    
    /**
     * Get current request count
     */
    public long getCurrentCount(String key) {
        String rateKey = RATE_LIMIT_PREFIX + key;
        Object count = redisTemplate.opsForValue().get(rateKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }
    
    /**
     * Reset rate limit for key
     */
    public void resetLimit(String key) {
        String rateKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(rateKey);
    }
    
    /**
     * Get time until reset
     */
    public long getTimeUntilReset(String key) {
        String rateKey = RATE_LIMIT_PREFIX + key;
        Long ttl = redisTemplate.getExpire(rateKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }
}