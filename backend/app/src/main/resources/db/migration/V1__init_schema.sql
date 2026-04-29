-- ============================================================
-- V1__init_schema.sql — kehdo initial schema
-- ============================================================
-- Creates: users, sessions, user_quotas, conversations,
--          messages, replies, tones
-- All tables use UUID v7 primary keys (time-ordered).
-- Soft delete via deleted_at; hard delete via nightly job.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Helper: enum-like via CHECK constraints (cleaner than Postgres enums for evolution)

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(72),  -- BCrypt; null for Google-only accounts
    display_name    VARCHAR(80),
    google_sub      VARCHAR(255) UNIQUE,
    plan            VARCHAR(20) NOT NULL DEFAULT 'STARTER'
                    CHECK (plan IN ('STARTER', 'PRO', 'UNLIMITED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_google_sub ON users(google_sub) WHERE google_sub IS NOT NULL;

-- ============================================================
-- SESSIONS (refresh tokens)
-- ============================================================
CREATE TABLE sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash  VARCHAR(255) NOT NULL UNIQUE,
    user_agent          VARCHAR(500),
    ip_address          INET,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at          TIMESTAMPTZ
);

CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_expires ON sessions(expires_at) WHERE revoked_at IS NULL;

-- ============================================================
-- USER QUOTAS (daily reply count for free tier)
-- ============================================================
CREATE TABLE user_quotas (
    user_id         UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    daily_used      INTEGER NOT NULL DEFAULT 0,
    daily_limit     INTEGER NOT NULL DEFAULT 5,
    reset_at        TIMESTAMPTZ NOT NULL,
    lifetime_count  BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- TONES (lookup table)
-- ============================================================
CREATE TABLE tones (
    code            VARCHAR(20) PRIMARY KEY,
    name            VARCHAR(40) NOT NULL,
    emoji           VARCHAR(10),
    description     TEXT,
    is_pro          BOOLEAN NOT NULL DEFAULT false,
    sort_order      INTEGER NOT NULL DEFAULT 0
);

INSERT INTO tones (code, name, emoji, description, is_pro, sort_order) VALUES
    ('CASUAL',     'Casual',     '💬', 'Relaxed, everyday',      false, 1),
    ('FLIRTY',     'Flirty',     '😉', 'Playful, charming',       false, 2),
    ('WITTY',      'Witty',      '✨', 'Sharp, clever',           false, 3),
    ('FORMAL',     'Formal',     '💼', 'Polished, professional',  false, 4),
    ('WARM',       'Warm',       '🙏', 'Sincere, kind',           false, 5),
    ('DIRECT',     'Direct',     '⚡', 'Clear, no-nonsense',      false, 6),
    ('SARCASTIC',  'Sarcastic',  '🎭', 'Dry, ironic',             true,  7),
    ('POETIC',     'Poetic',     '🖋', 'Literary, evocative',     true,  8),
    ('GRATEFUL',   'Grateful',   '💕', 'Appreciative, heartfelt', true,  9),
    ('THOUGHTFUL', 'Thoughtful', '💭', 'Considered, deep',        true,  10),
    ('CONFIDENT',  'Confident',  '👑', 'Self-assured',            true,  11),
    ('PLAYFUL',    'Playful',    '🎈', 'Fun, light',              true,  12);

-- ============================================================
-- CONVERSATIONS
-- ============================================================
CREATE TABLE conversations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    s3_key          VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING_UPLOAD'
                    CHECK (status IN ('PENDING_UPLOAD', 'PROCESSING', 'READY', 'FAILED')),
    failure_reason  VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_conversations_user_created ON conversations(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- ============================================================
-- MESSAGES (parsed from screenshots)
-- ============================================================
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    speaker         VARCHAR(10) NOT NULL CHECK (speaker IN ('ME', 'THEM')),
    text            TEXT NOT NULL,
    confidence      NUMERIC(3,2),
    sequence        INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, sequence);

-- ============================================================
-- REPLIES (generated by AI)
-- ============================================================
CREATE TABLE replies (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    rank            INTEGER NOT NULL CHECK (rank BETWEEN 1 AND 5),
    text            VARCHAR(500) NOT NULL,
    tone_code       VARCHAR(20) NOT NULL REFERENCES tones(code),
    model_used      VARCHAR(50),
    is_favorited    BOOLEAN NOT NULL DEFAULT false,
    is_copied       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_replies_conversation ON replies(conversation_id, rank);
CREATE INDEX idx_replies_favorites ON replies(conversation_id) WHERE is_favorited = true;

-- ============================================================
-- updated_at triggers
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_user_quotas_updated_at BEFORE UPDATE ON user_quotas
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_conversations_updated_at BEFORE UPDATE ON conversations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
