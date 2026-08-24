-- Enable required extensions for DocuSync
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgvector";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Verify installations
SELECT extname, extversion 
FROM pg_extension 
WHERE extname IN ('uuid-ossp', 'vector', 'pg_trgm');