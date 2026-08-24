-- =============================================
-- Document Chunks for Vector Storage
-- =============================================
CREATE EXTENSION IF NOT EXISTS "vector";

CREATE TABLE document_chunks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER NOT NULL,
    embedding vector(1536),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_document_chunk UNIQUE (document_id, chunk_index)
);

-- Create HNSW index for fast similarity search
CREATE INDEX idx_document_chunks_embedding 
    ON document_chunks 
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 200);

-- Create index for document_id lookups
CREATE INDEX idx_document_chunks_document_id 
    ON document_chunks(document_id);

-- Create index for chunk ordering
CREATE INDEX idx_document_chunks_order 
    ON document_chunks(document_id, chunk_index);