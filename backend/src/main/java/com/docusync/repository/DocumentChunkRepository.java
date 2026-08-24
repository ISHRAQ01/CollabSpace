package com.docusync.repository;

import com.docusync.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * DocumentChunk Repository
 * 
 * Data access layer for RAG (Retrieval-Augmented Generation) chunks
 */
@Repository
public interface DocumentChunkRepository 
        extends JpaRepository<DocumentChunk, UUID> {
    
    /**
     * Find all chunks for a document (ordered)
     */
    @Query("""
           SELECT dc FROM DocumentChunk dc 
           WHERE dc.documentId = :documentId 
           ORDER BY dc.chunkIndex ASC
           """)
    List<DocumentChunk> findByDocumentIdOrdered(
            @Param("documentId") UUID documentId);
    
    /**
     * Find chunks with vector similarity search
     */
    @Query(value = """
           SELECT dc.*, 
                  1 - (dc.embedding <=> CAST(:queryEmbedding AS vector)) as similarity
           FROM document_chunks dc
           WHERE dc.document_id = :documentId
           ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector)
           LIMIT :limit
           """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("documentId") UUID documentId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit);
    
    /**
     * Delete all chunks for a document
     */
    @Modifying
    @Query("DELETE FROM DocumentChunk dc WHERE dc.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") UUID documentId);
    
    /**
     * Get total token count for a document
     */
    @Query("""
           SELECT SUM(dc.tokenCount) FROM DocumentChunk dc 
           WHERE dc.documentId = :documentId
           """)
    Long getTotalTokenCount(@Param("documentId") UUID documentId);
}