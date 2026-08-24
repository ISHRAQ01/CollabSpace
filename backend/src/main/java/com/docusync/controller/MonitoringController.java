package com.docusync.controller;

import com.docusync.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Monitoring Controller
 * 
 * Provides monitoring and diagnostic endpoints
 */
@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "Monitoring and diagnostics endpoints")
public class MonitoringController {
    
    private final MetricsService metricsService;
    
    /**
     * Get system status
     */
    @GetMapping("/status")
    @Operation(summary = "Get system status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = Map.of(
                "application", "DocuSync Enterprise Engine",
                "version", "1.0.0",
                "status", "running",
                "timestamp", java.time.Instant.now().toString(),
                "javaVersion", System.getProperty("java.version"),
                "availableProcessors", Runtime.getRuntime().availableProcessors(),
                "freeMemory", Runtime.getRuntime().freeMemory(),
                "totalMemory", Runtime.getRuntime().totalMemory(),
                "maxMemory", Runtime.getRuntime().maxMemory()
        );
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get metrics summary
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get metrics summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> metrics = Map.of(
                "status", "healthy",
                "timestamp", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(metrics);
    }
}