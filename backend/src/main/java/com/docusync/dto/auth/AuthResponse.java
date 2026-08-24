package com.docusync.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private UUID userId;
    private String email;
    private String username;
    private String fullName;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
}