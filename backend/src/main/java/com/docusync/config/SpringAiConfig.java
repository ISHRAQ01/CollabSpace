package com.docusync.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring AI Configuration
 * 
 * Configures vector store and AI models for RAG implementation
 */
@Configuration
public class SpringAiConfig {
    
    @Value("${spring.ai.vector-store.dimensions}")
    private int vectorDimensions;
    
    @Value("${spring.ai.vector-store.index-type}")
    private String indexType;
    
    /**
     * PostgreSQL Vector Store for document embeddings
     */
    @Bean
    public VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel) {
        
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorDimensions(vectorDimensions)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .build();
    }
}