-- =============================================
-- DocuSync Initial Database Schema
-- =============================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================
-- Users Table
-- =============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN', 'SYSTEM'))
);

-- =============================================
-- Documents Table
-- =============================================
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    content TEXT,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    current_version BIGINT NOT NULL DEFAULT 1,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    optimistic_lock_version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_created_by ON documents(created_by);
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
CREATE INDEX idx_documents_title_trgm ON documents USING gin (title gin_trgm_ops);

-- =============================================
-- Document Collaborators Table
-- =============================================
CREATE TABLE document_collaborators (
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (document_id, user_id),
    CONSTRAINT chk_collaborators_role 
        CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER', 'COMMENTATOR'))
);

CREATE INDEX idx_collab_user_id ON document_collaborators(user_id);
CREATE INDEX idx_collab_document_id ON document_collaborators(document_id);

-- =============================================
-- Version History Table
-- =============================================
CREATE TABLE version_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    version_number BIGINT NOT NULL,
    content_snapshot TEXT NOT NULL,
    change_summary VARCHAR(500),
    change_size BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_version_document_number 
        UNIQUE (document_id, version_number)
);

CREATE INDEX idx_version_document_id ON version_history(document_id);
CREATE INDEX idx_version_created_at ON version_history(created_at DESC);

-- =============================================
-- Updated_at Trigger Function
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to relevant tables
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documents_updated_at 
    BEFORE UPDATE ON documents 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_collaborators_updated_at 
    BEFORE UPDATE ON document_collaborators 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Initial System User
-- =============================================
INSERT INTO users (id, email, username, password_hash, full_name, role, is_verified)
VALUES (
    uuid_generate_v4(),
    'system@docusync.io',
    'system',
    'SYSTEM_ACCOUNT_NO_LOGIN',
    'DocuSync System',
    'SYSTEM',
    TRUE
);