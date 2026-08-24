# DocuSync Enterprise Engine
### Distributed, Real-Time Collaborative Workspace Engine with CRDT Conflict Resolution & RAG AI
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.0.0_M1-blue.svg?style=flat-square&logo=spring)](https://spring.io/projects/spring-ai)
[![Postgres](https://img.shields.io/badge/PostgreSQL-16%20%7C%20pgvector-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg?style=flat-square&logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

DocuSync Enterprise is a production-grade, highly scalable real-time collaborative document engine. It uses conflict-free data synchronization and contextual artificial intelligence to support thousands of concurrent sessions per document. 

This engine is built on **Java 21 (using Project Loom Virtual Threads)**, **Spring Boot 3.3+**, and **Spring AI**. It resolves state conflicts locally on the client using **Yjs CRDTs**, while a distributed cluster of Spring Boot nodes handles routing, updates, and persistence. The backend uses a custom sync protocol to manage edits in memory, while a **Redis Pub/Sub** network scales operations horizontally. Additionally, document context is vectorized and stored in **PostgreSQL (via pgvector)** to power a context-aware Retrieval-Augmented Generation (RAG) assistant.

---

## Architecture Blueprint

```
                                 +---------------------------------------+
                                 |          Next.js Client App           |
                                 |   (Lexical Editor + Y.js CRDT Client) |
                                 +------------------+--------------------+
                                                    |
                                  HTTPS (REST APIs) | WebSockets (Binary CRDT Stream)
                                                    v
                                 +------------------+--------------------+
                                 |         Nginx / Cloudflare ALB        |
                                 +------------------+--------------------+
                                                    |
                      +-----------------------------+-----------------------------+
                      | (Sticky Sessions or Round-Robin w/ PubSub)                |
                      v                                                           v
+---------------------+---------------------+               +---------------------+---------------------+
|      DocuSync Boot Node 1 (JVM)            |               |      DocuSync Boot Node 2 (JVM)            |
|  - Spring WebSockets (Virtual Threads)     |               |  - Spring WebSockets (Virtual Threads)     |
|  - Y-Java CRDT State Merge Engine          |               |  - Y-Java CRDT State Merge Engine          |
|  - Spring AI Retrieval Pipelines           |               |  - Spring AI Retrieval Pipelines           |
+----------+----------------------+----------+               +----------+----------------------+----------+
           |                      |                                     |                      |
           | Redis Pub/Sub        | JDBC Connection                     | Redis Pub/Sub        | JDBC Connection
           v                      v                                     v                      v
+----------+----------------------+----------+               +----------+----------------------+----------+
|          Redis Cluster (Cache)             |               |             PostgreSQL 16               |
|  - Shared WebSocket Session Registry       |               |  - Relational Transactional Schemas       |
|  - Distributed Pub/Sub Messaging Bus       |               |  - pgvector Storage Engine                |
|  - Redisson Distributed Locks              |               |  - HNSW Approximate Nearest Neighbor Index  |
+--------------------------------------------+               +----------+-----------------------------+
                                                                        |
                                                                        | Local/Cloud LLM API Integration
                                                                        v
                                                             +----------+-----------------------------+
                                                             |        Spring AI / LLM Gateway         |
                                                             |  - Local Ollama (Llama 3 / Mistral)    |
                                                             |  - OpenAI GPT-4o Integration            |
                                                             +----------------------------------------+
```

---

## Design & Engineering Highlights

### 🚀 High-Concurrency WebSocket Scaling (Project Loom)
In traditional servlet architectures, each WebSocket connection locks up a physical operating system thread. This model caps connection limits early due to memory overhead.
* **Virtual Threads Integration:** DocuSync delegates connection handling to lightweight Java Virtual Threads.
* **Efficiency Gains:** The application can handle more than 10,000 concurrent, long-lived WebSocket sessions per node with minimal context-switching overhead and low memory usage.

### 🔄 Distributed CRDT Synchronization & Merges
State-merging and reconciliation are performed directly within the JVM.
* **State Updates:** We capture binary-level state changes from Yjs editor sessions and stream them to the server over WebSockets.
* **Merging and Conflicts:** In-memory CRDT updates are processed and merged using an optimized Java synchronization layer. This ensures document states converge across all active client screens without requiring central processing.

### 🧠 Semantic Retrieval-Augmented Generation (RAG) with Spring AI
An AI writing assistant parses, indexes, and queries the document's content in real time.
* **Pipeline Processing:** A debounced background pipeline extracts document text, splits it into semantic chunks, and generates vector embeddings.
* **Vector Lookups:** Chunks are saved to PostgreSQL with `pgvector` and indexed using Hierarchical Navigable Small World (**HNSW**) indexes.
* **Semantic Search:** When a user queries the document, the app performs a high-speed cosine-similarity lookup to build a contextually rich system prompt, which is then completed by **Spring AI**.

### ⚡ Horizontally Scalable Architecture
State updates are synchronized across different application instances to support horizontal scaling.
* **Redis Pub/Sub:** When Node A processes a document update, it broadcasts the change to a dedicated channel in the Redis cluster.
* **Node Synchronization:** Other nodes subscribed to the channel pick up the update and forward it to their active WebSocket clients.
* **Rate Limiting & Locks:** Redisson handles distributed locks to prevent write conflicts when periodically saving document updates back to PostgreSQL.

---

## Project Structure

```
docusync-parent/
│
├── docusync-api-gateway/                 # Infrastructure routing and rate limiting configuration
│
└── docusync-core/                        # Main Spring Boot Service
    ├── src/
    │   ├── main/
    │   │   ├── java/com/docusync/core/
    │   │   │   ├── DocuSyncApplication.java
    │   │   │   │
    │   │   │   ├── config/               # Infrastructure, security, and thread pools
    │   │   │   │   ├── WebSocketConfig.java
    │   │   │   │   ├── RedisConfig.java
    │   │   │   │   ├── SpringAiConfig.java
    │   │   │   │   └── ThreadPoolConfig.java # Virtual Thread Executor setup
    │   │   │   │
    │   │   │   ├── domain/               # Core entities and data mappers
    │   │   │   │   ├── document/
    │   │   │   │   │   ├── model/Document.java
    │   │   │   │   │   ├── model/Collaborator.java
    │   │   │   │   │   ├── model/VersionHistory.java
    │   │   │   │   │   └── repository/DocumentRepository.java
    │   │   │   │   ├── user/
    │   │   │   │   │   ├── model/User.java
    │   │   │   │   │   └── repository/UserRepository.java
    │   │   │   │   └── ai/
    │   │   │   │       ├── model/DocumentChunk.java
    │   │   │   │       └── repository/VectorChunkRepository.java
    │   │   │   │
    │   │   │   ├── service/              # Business layer
    │   │   │   │   ├── sync/
    │   │   │   │   │   ├── CollaborativeSyncService.java   # CRDT state reconciliation
    │   │   │   │   │   └── DistributedPubSubService.java   # Redis message broker
    │   │   │   │   ├── document/
    │   │   │   │   │   └── DocumentService.java            # Permissions, history, saves
    │   │   │   │   └── ai/
    │   │   │   │       └── RagEngineService.java           # Vector indexing, Spring AI pipeline
    │   │   │   │
    │   │   │   └── web/                  # API endpoints and entry points
    │   │   │       ├── rest/
    │   │   │       │   ├── DocumentRestController.java
    │   │   │       │   └── AiAssistantRestController.java
    │   │   │       └── websocket/
    │   │   │           ├── RealtimeSessionHandler.java      # Raw WebSocket server
    │   │   │           └── dto/WebSocketFrameDto.java
    │   │   │
    │   │   └── resources/
    │   │       ├── application.yml       # DB, Redis, and Spring AI configurations
    │   │       └── db/migration/
    │   │           └── V1__initial_schema.sql  # Database migrations
    │   │
    │   └── test/                         # Unit and concurrency integration tests
```

---

## Setup & Local Installation

### Prerequisites
* **Java SDK 21** or higher (e.g., GraalVM, Amazon Corretto, OpenJDK)
* **Maven 3.9+**
* **Docker & Docker Compose**

### 1. Launch Services
Run the following command to start PostgreSQL (with pgvector), Redis, and Ollama:

```bash
docker-compose -f docker/docker-compose.yml up -d
```

### 2. Configure Local AI Embeddings (Optional)
If you are running models locally via Ollama:

```bash
# Pull Llama 3 for text generation
docker exec -it ollama ollama run llama3

# Pull nomic-embed-text for vector embeddings
docker exec -it ollama ollama pull nomic-embed-text
```

### 3. Build the Application
Navigate to the root directory and run a Maven build to verify dependencies and run the automated test suite:

```bash
mvn clean install
```

### 4. Configure Application Properties
Edit the environment variables in `docusync-core/src/main/resources/application.yml` or supply them directly on startup:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/docusync_db
    username: postgres
    password: supersecretpassword
  redis:
    host: localhost
    port: 6379
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
```

### 5. Start the Engine
Start the Spring Boot application using Maven:

```bash
mvn spring-boot:run -pl docusync-core
```

---

## API Specifications

### REST APIs

#### 1. Create Collaborative Document
* **Endpoint:** `POST /api/v1/documents`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "title": "Systems Architecture Whitepaper"
  }
  ```
* **Response Body (`201 Created`):**
  ```json
  {
    "id": "e96f1f41-b21a-42c2-8fe2-8c1d5bfe8bfa",
    "title": "Systems Architecture Whitepaper",
    "createdAt": "2024-11-20T10:15:30Z",
    "updatedAt": "2024-11-20T10:15:30Z"
  }
  ```

#### 2. Query Document Context (RAG AI)
* **Endpoint:** `POST /api/v1/ai/query`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "documentId": "e96f1f41-b21a-42c2-8fe2-8c1d5bfe8bfa",
    "prompt": "Summarize our planned database scaling strategy."
  }
  ```
* **Response Body (`200 OK`):**
  ```json
  {
    "documentId": "e96f1f41-b21a-42c2-8fe2-8c1d5bfe8bfa",
    "answer": "Our scaling strategy centers on using PostgreSQL with HNSW vector indexing for vector lookups and implementing Redis Pub/Sub to scale WebSocket connections horizontally.",
    "tokensUsed": 245,
    "sources": [
      {
        "chunkId": "ca927d35-f09c-43ba-afbe-a6984e9c0dbf",
        "previewText": "...database scaling strategies include using PostgreSQL and HNSW index structures..."
      }
    ]
  }
  ```

---

### WebSocket Sync API
The WebSocket endpoint is located at `ws://localhost:8080/sync/{documentId}`. It handles synchronization using raw binary frames to keep serialization overhead low.

#### Frame Types
To minimize parsing delays, all client-to-server frames start with a **1-byte event prefix**:

| Byte Prefix | Message Type | Description |
| :--- | :--- | :--- |
| `0x00` | **Sync Step 1** | Sent by clients to transmit their current Yjs state vector. |
| `0x01` | **Sync Step 2** | Returned by the server to send missing state deltas. |
| `0x02` | **Incremental Sync**| Real-time delta changes containing character insertions, deletions, and structural document changes. |
| `0x03` | **Awareness Context**| JSON payload containing live mouse cursor coordinates, text selections, and user details. |

---

## Production Readiness Checklist

Before moving to production, complete the following setup steps:

* [ ] **Tuning Project Loom:** Set `-Djdk.virtualThreadScheduler.maxPoolSize` to match your targeted container core allocations.
* [ ] **SSL/TLS Setup:** Terminate WebSocket connections (`wss://`) at your load balancer level to reduce CPU overhead on the JVM nodes.
* [ ] **Persistent Storage & HNSW Tuning:** Adjust the HNSW index settings (`m` and `ef_construction` parameters in `pgvector`) depending on your expected vector collection sizes.
* [ ] **Connection Heartbeats:** Keep the active WebSocket connection timeout set to 60 seconds to avoid unexpected connection resets in Nginx or Cloudflare.

---

## License
Distributed under the MIT License. See `LICENSE` for details.
