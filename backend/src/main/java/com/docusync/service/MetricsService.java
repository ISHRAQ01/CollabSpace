package com.docusync.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Metrics Service
 * 
 * Tracks custom application metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Custom metrics
    private final Counter documentCreations;
    private final Counter documentUpdates;
    private final Counter documentDeletions;
    private final Counter aiQueries;
    private final Timer documentOperationTimer;
    private final Timer aiQueryTimer;
    
    /**
     * Initialize metrics
     */
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.documentCreations = Counter.builder("docusync.documents.created")
                .description("Number of documents created")
                .register(meterRegistry);
        
        this.documentUpdates = Counter.builder("docusync.documents.updated")
                .description("Number of document updates")
                .register(meterRegistry);
        
        this.documentDeletions = Counter.builder("docusync.documents.deleted")
                .description("Number of documents deleted")
                .register(meterRegistry);
        
        this.aiQueries = Counter.builder("docusync.ai.queries")
                .description("Number of AI queries")
                .register(meterRegistry);
        
        this.documentOperationTimer = Timer.builder("docusync.documents.operation.time")
                .description("Time taken for document operations")
                .register(meterRegistry);
        
        this.aiQueryTimer = Timer.builder("docusync.ai.query.time")
                .description("Time taken for AI queries")
                .register(meterRegistry);
    }
    
    /**
     * Record document creation
     */
    public void recordDocumentCreation() {
        documentCreations.increment();
    }
    
    /**
     * Record document update
     */
    public void recordDocumentUpdate() {
        documentUpdates.increment();
    }
    
    /**
     * Record document deletion
     */
    public void recordDocumentDeletion() {
        documentDeletions.increment();
    }
    
    /**
     * Record AI query
     */
    public void recordAIQuery() {
        aiQueries.increment();
    }
    
    /**
     * Time document operation
     */
    public <T> T timeDocumentOperation(Supplier<T> operation) {
        return documentOperationTimer.record(operation);
    }
    
    /**
     * Time AI query
     */
    public <T> T timeAIQuery(Supplier<T> operation) {
        return aiQueryTimer.record(operation);
    }
    
    /**
     * Record custom counter
     */
    public void incrementCounter(String name, String... tags) {
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record gauge value
     */
    public void recordGauge(String name, double value, String... tags) {
        meterRegistry.gauge(name, io.micrometer.core.instrument.Tags.of(tags), value);
    }
    
    /**
     * Get active WebSocket connections count
     */
    public void updateWebSocketConnections(int count) {
        recordGauge("docusync.websocket.connections", count);
    }
    
    /**
     * Get active documents count
     */
    public void updateActiveDocuments(int count) {
        recordGauge("docusync.documents.active", count);
    }
}