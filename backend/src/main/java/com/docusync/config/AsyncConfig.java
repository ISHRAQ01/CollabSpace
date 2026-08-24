package com.docusync.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Async Configuration
 * 
 * Configures async execution with proper exception handling
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
    
    /**
     * Custom exception handler for async operations
     */
    private static class CustomAsyncExceptionHandler 
            implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(
                Throwable throwable, 
                Method method, 
                Object... params) {
            
            log.error("Async operation failed in method: {}", method.getName());
            log.error("Parameters: {}", Arrays.toString(params));
            log.error("Exception: ", throwable);
        }
    }
}