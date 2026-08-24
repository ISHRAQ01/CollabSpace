package com.docusync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed Lock Service
 * 
 * Provides distributed locking for concurrent operations
 * Uses Redisson for Redis-based distributed locks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {
    
    private final RedissonClient redissonClient;
    
    private static final String DOCUMENT_LOCK_PREFIX = "lock:document:";
    private static final String USER_LOCK_PREFIX = "lock:user:";
    private static final long DEFAULT_WAIT_TIME = 5;
    private static final long DEFAULT_LEASE_TIME = 30;
    
    /**
     * Execute operation with document lock
     */
    public <T> T executeWithDocumentLock(
            String documentId, 
            Supplier<T> operation) {
        
        String lockKey = DOCUMENT_LOCK_PREFIX + documentId;
        return executeWithLock(lockKey, operation);
    }
    
    /**
     * Execute operation with document lock and custom timeouts
     */
    public <T> T executeWithDocumentLock(
            String documentId, 
            Supplier<T> operation,
            long waitTime,
            long leaseTime,
            TimeUnit timeUnit) {
        
        String lockKey = DOCUMENT_LOCK_PREFIX + documentId;
        return executeWithLock(lockKey, operation, waitTime, leaseTime, timeUnit);
    }
    
    /**
     * Execute operation with user lock
     */
    public <T> T executeWithUserLock(
            String userId, 
            Supplier<T> operation) {
        
        String lockKey = USER_LOCK_PREFIX + userId;
        return executeWithLock(lockKey, operation);
    }
    
    /**
     * Execute operation with distributed lock
     */
    private <T> T executeWithLock(
            String lockKey, 
            Supplier<T> operation) {
        
        return executeWithLock(
                lockKey, 
                operation, 
                DEFAULT_WAIT_TIME, 
                DEFAULT_LEASE_TIME, 
                TimeUnit.SECONDS);
    }
    
    /**
     * Execute operation with distributed lock and custom timeouts
     */
    private <T> T executeWithLock(
            String lockKey, 
            Supplier<T> operation,
            long waitTime,
            long leaseTime,
            TimeUnit timeUnit) {
        
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock
            boolean locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            
            if (!locked) {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new RuntimeException("Failed to acquire lock: " + lockKey);
            }
            
            log.debug("Acquired lock: {}", lockKey);
            
            // Execute operation
            return operation.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for lock: " + lockKey);
        } finally {
            // Release lock
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released lock: {}", lockKey);
            }
        }
    }
    
    /**
     * Check if lock is held
     */
    public boolean isLocked(String documentId) {
        String lockKey = DOCUMENT_LOCK_PREFIX + documentId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
    
    /**
     * Force unlock (for administrative purposes)
     */
    public void forceUnlock(String documentId) {
        String lockKey = DOCUMENT_LOCK_PREFIX + documentId;
        RLock lock = redissonClient.getLock(lockKey);
        
        if (lock.isLocked()) {
            lock.forceUnlock();
            log.warn("Force unlocked: {}", lockKey);
        }
    }
}