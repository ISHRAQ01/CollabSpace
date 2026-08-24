package com.docusync.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test Configuration
 * 
 * Provides test-specific beans and configurations
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Password encoder for tests (faster hashing)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);  // Weaker for faster tests
    }
}