package com.docusync.service;

import com.docusync.config.BaseIntegrationTest;
import com.docusync.dto.auth.AuthResponse;
import com.docusync.dto.auth.LoginRequest;
import com.docusync.dto.auth.RegisterRequest;
import com.docusync.exception.AuthenticationException;
import com.docusync.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Auth Service Integration Test
 */
@Sql(scripts = "/db/test-data.sql")
class AuthServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private AuthService authService;
    
    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@test.com")
                .username("newuser")
                .password("Password123!")
                .fullName("New User")
                .build();
        
        // When
        AuthResponse response = authService.register(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@test.com");
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }
    
    @Test
    @DisplayName("Should throw exception for duplicate email")
    void shouldThrowExceptionForDuplicateEmail() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test1@docusync.io")  // Already exists
                .username("newuser")
                .password("Password123!")
                .build();
        
        // When/Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
    
    @Test
    @DisplayName("Should login with valid credentials")
    void shouldLoginWithValidCredentials() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .identifier("test1@docusync.io")
                .password("password123")
                .build();
        
        // When
        AuthResponse response = authService.login(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test1@docusync.io");
        assertThat(response.getAccessToken()).isNotBlank();
    }
    
    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void shouldThrowExceptionForInvalidCredentials() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .identifier("test1@docusync.io")
                .password("wrongpassword")
                .build();
        
        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class);
    }
}