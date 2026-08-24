package com.docusync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DocuSync Enterprise Engine - Main Application Entry Point
 * 
 * Distributed Real-Time Collaborative Workspace with CRDT & RAG AI
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DocusyncApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DocusyncApplication.class, args);
    }
}