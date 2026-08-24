package com.docusync.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API Documentation Controller
 * 
 * Provides API information and documentation endpoints
 */
@RestController
@RequestMapping("/api-docs")
@Tag(name = "API Documentation", description = "API information endpoints")
public class ApiDocumentationController {
    
    /**
     * Get API information
     */
    @GetMapping("/info")
    @Operation(summary = "Get API information")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = Map.of(
                "name", "DocuSync Enterprise Engine API",
                "version", "1.0.0",
                "status", "active",
                "documentation", "/swagger-ui.html",
                "openapi", "/v3/api-docs"
        );
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Get API health status
     */
    @GetMapping("/status")
    @Operation(summary = "Get API status")
    public ResponseEntity<Map<String, String>> getApiStatus() {
        Map<String, String> status = Map.of(
                "status", "operational",
                "timestamp", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(status);
    }
}