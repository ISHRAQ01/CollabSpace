package com.docusync.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends DocusyncException {
    
    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
    
    public RateLimitExceededException(String resource, int maxRequests, long windowSeconds) {
        super(
            String.format("Rate limit exceeded for %s. Maximum %d requests per %d seconds", 
                         resource, maxRequests, windowSeconds),
            HttpStatus.TOO_MANY_REQUESTS,
            "RATE_LIMIT_EXCEEDED",
            new RateLimitDetails(resource, maxRequests, windowSeconds)
        );
    }
    
    private record RateLimitDetails(String resource, int maxRequests, long windowSeconds) {}
}