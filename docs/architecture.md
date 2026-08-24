# DocuSync Architecture

## High-Level Overview
Client Layer │
│ (React/Next.js + Yjs CRDT) │
└─────────────────┬───────────────────────────────────┘
│ WebSocket/REST
┌─────────────────▼───────────────────────────────────┐
│ Application Layer │
│ Spring Boot 3.3 + Java 21 Virtual Threads │
└─────┬──────────────────────────┬───────────────────┘
│ │
┌─────▼─────────┐ ┌───────────▼──────────────┐
│ PostgreSQL │ │ Redis │
│ + pgvector │ │ (Pub/Sub + Cache) │
└───────────────┘ └──────────────────────────┘


## Key Design Decisions

### 1. Virtual Threads for WebSocket Handling
- Each WebSocket connection runs on a Java Virtual Thread
- Supports 10K+ concurrent connections per node

### 2. CRDT-based Conflict Resolution
- Yjs protocol for client-side state management
- Server-side merge & broadcast via Redis Pub/Sub

### 3. RAG Pipeline
- Document chunks → Embeddings → pgvector storage
- HNSW index for fast similarity search
- Spring AI for LLM orchestration

## Scaling Strategy
- Horizontal scaling via Redis Pub/Sub
- Sticky sessions optional (stateless design)
- Database sharding strategy (future)