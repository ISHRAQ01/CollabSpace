-- Test Data for Integration Tests

-- Create test users
INSERT INTO users (id, email, username, password_hash, full_name, role, is_verified)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'test1@docusync.io', 'testuser1', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYI6R9C0LrO', 
     'Test User 1', 'USER', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'test2@docusync.io', 'testuser2', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYI6R9C0LrO', 
     'Test User 2', 'USER', TRUE),
    ('33333333-3333-3333-3333-333333333333', 'admin@docusync.io', 'adminuser', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYI6R9C0LrO', 
     'Admin User', 'ADMIN', TRUE);

-- Create test documents
INSERT INTO documents (id, title, description, content, is_public, current_version, created_by)
VALUES 
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Test Document 1', 
     'Test document for integration testing', 
     'This is the content of test document 1.', FALSE, 1, 
     '11111111-1111-1111-1111-111111111111'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Public Document', 
     'Public document for testing', 
     'This is a public document.', TRUE, 2, 
     '22222222-2222-2222-2222-222222222222');

-- Create test collaborators
INSERT INTO document_collaborators (document_id, user_id, role)
VALUES 
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'OWNER'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'EDITOR'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'OWNER');

-- Create version history
INSERT INTO version_history (id, document_id, created_by, version_number, content_snapshot, change_summary, change_size)
VALUES 
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     '22222222-2222-2222-2222-222222222222',
     1, 'Initial content', 'Initial creation', 15);