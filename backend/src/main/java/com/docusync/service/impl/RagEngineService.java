package com.docusync.service.impl;

import com.docusync.entity.Document;
import com.docusync.entity.DocumentChunk;
import com.docusync.repository.DocumentChunkRepository;
import com.docusync.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG Engine Service
 * 
 * Implements Retrieval-Augmented Generation for document intelligence
 * Uses Spring AI for embedding and chat operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEngineService {
    
    private final EmbeddingModel embeddingModel;
private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;
    
    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;
    private static final int MAX_CONTEXT_CHUNKS = 5;
    
    /**
     * Index document for RAG
     */
    @Transactional
    public void indexDocument(UUID documentId) {
        log.info("Indexing document {} for RAG", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Remove existing chunks
        chunkRepository.deleteByDocumentId(documentId);
        
        // Split content into chunks
        List<String> chunks = splitIntoChunks(document.getContent());
        
        // Process each chunk
        List<DocumentChunk> documentChunks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            
            // Generate embedding
            float[] embedding = generateEmbedding(chunkContent);
            
            // Create chunk entity
            DocumentChunk chunk = DocumentChunk.builder()
                    .documentId(documentId)
                    .chunkIndex(i)
                    .content(chunkContent)
                    .tokenCount(estimateTokenCount(chunkContent))
                    .embedding(embedding)
                    .metadata(Map.of(
                            "documentId", documentId.toString(),
                            "chunkIndex", i,
                            "totalChunks", chunks.size()
                    ))
                    .build();
            
            documentChunks.add(chunk);
        }
        
        // Save all chunks
        chunkRepository.saveAll(documentChunks);
        
        log.info("Document {} indexed with {} chunks", documentId, chunks.size());
    }
    
    /**
     * Query document with RAG
     */
    public String queryWithContext(UUID documentId, String query) {
        log.debug("Querying document {} with: {}", documentId, query);
        
        // Generate query embedding
        float[] queryEmbedding = generateEmbedding(query);
        
        // Find similar chunks
        List<DocumentChunk> relevantChunks = findSimilarChunks(
                documentId, queryEmbedding, MAX_CONTEXT_CHUNKS);
        
        if (relevantChunks.isEmpty()) {
            return "No relevant context found in document.";
        }
        
        // Build context from chunks
        String context = relevantChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n"));
        
        // Create prompt with context
        PromptTemplate template = new PromptTemplate("""
                You are an AI assistant helping with document analysis.
                
                Document Context:
                {context}
                
                User Question: {question}
                
                Please provide a detailed answer based on the context above.
                If the context doesn't contain relevant information, say so.
                
                Answer:
                """);
        
        Map<String, Object> model = Map.of(
                "context", context,
                "question", query
        );
        
        Prompt prompt = template.create(model);
        
        // Get response from LLM
        String response = chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
        
        return response;
    }
    
    /**
     * Generate document summary
     */
    public String generateSummary(UUID documentId) {
        log.info("Generating summary for document {}", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        PromptTemplate template = new PromptTemplate("""
                Please provide a comprehensive summary of the following document:
                
                {content}
                
                Summary should include:
                1. Main topics and themes
                2. Key points and findings
                3. Important conclusions
                4. Recommended actions (if any)
                
                Keep the summary clear and well-structured.
                """);
        
        Map<String, Object> model = Map.of("content", document.getContent());
        Prompt prompt = template.create(model);
        
        return chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
    }
    
    /**
     * Generate action items from document
     */
    public List<String> generateActionItems(UUID documentId) {
        log.info("Generating action items for document {}", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        PromptTemplate template = new PromptTemplate("""
                Based on the following document, identify and list all action items:
                
                {content}
                
                Format each action item as a clear, actionable task.
                Include who might be responsible and any deadlines mentioned.
                """);
        
        Map<String, Object> model = Map.of("content", document.getContent());
        Prompt prompt = template.create(model);
        
        String response = chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
        
        // Parse response into list
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Improve writing for content
     */
    public String improveWriting(String content) {
        log.debug("Improving writing for content");
        
        PromptTemplate template = new PromptTemplate("""
                Please improve the following text:
                
                {content}
                
                Improvements should include:
                1. Better clarity and conciseness
                2. Improved grammar and punctuation
                3. More professional tone
                4. Better sentence structure
                
                Return only the improved text without explanations.
                """);
        
        Map<String, Object> model = Map.of("content", content);
        Prompt prompt = template.create(model);
        
        return chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
    }
    
    /**
     * Split content into chunks
     */
    private List<String> splitIntoChunks(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> chunks = new ArrayList<>();
        String[] words = content.split("\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;
        
        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;
            
            if (wordCount >= CHUNK_SIZE) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                wordCount = 0;
            }
        }
        
        // Add remaining chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * Generate embedding for text
     */
    private float[] generateEmbedding(String text) {
        try {
            var embedding = embeddingClient.embed(text);
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i);
            }
            return vector;
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage());
            return new float[1536]; // Return zero vector
        }
    }
    
    /**
     * Find similar chunks using vector search
     */
    private List<DocumentChunk> findSimilarChunks(
            UUID documentId, 
            float[] queryEmbedding, 
            int limit) {
        
        // Convert embedding to string for SQL query
        String embeddingString = Arrays.toString(queryEmbedding);
        
        return chunkRepository.findSimilarChunks(
                documentId, embeddingString, limit);
    }
    
    /**
     * Estimate token count
     */
    private int estimateTokenCount(String text) {
        // Rough estimation: 1 token ≈ 4 characters
        return text.length() / 4;
    }
}