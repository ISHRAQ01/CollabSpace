package com.docusync.service;

import com.docusync.config.JwtUtil;
import com.docusync.dto.auth.AuthResponse;
import com.docusync.dto.auth.LoginRequest;
import com.docusync.dto.auth.RegisterRequest;
import com.docusync.entity.User;
import com.docusync.exception.AuthenticationException;
import com.docusync.exception.DuplicateResourceException;
import com.docusync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * Handles user registration, login, and token management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        // Check for existing email
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        
        // Check for existing username
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        
        // Create new user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .isVerified(false)
                .role(User.UserRole.USER)
                .build();
        
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(), user.getEmail());
        
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    /**
     * Authenticate user
     */
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getIdentifier());
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(), 
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("Login failed for user: {}", request.getIdentifier());
            throw new AuthenticationException("Invalid credentials");
        }
        
        User user = userRepository.findByEmailIgnoreCase(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(request.getIdentifier())
                        .orElseThrow(() -> new AuthenticationException("User not found")));
        
        // Update last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(), user.getEmail());
        
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    /**
     * Refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        if (!jwtUtil.validateToken(refreshToken, 
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .build())) {
            throw new AuthenticationException("Expired refresh token");
        }
        
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(
                user.getId(), user.getEmail());
        
        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }
    
    /**
     * Build authentication response
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400) // 24 hours
                .tokenType("Bearer")
                .build();
    }
}