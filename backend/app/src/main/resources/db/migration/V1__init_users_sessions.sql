-- ============================================================================
-- V1 — initial schema: users + sessions
-- ============================================================================
-- UUID primary keys are generated client-side (UUIDv7) per backend/CLAUDE.md
-- "UUID v7 for all primary keys (time-ordered, avoids enumeration)".
-- We don't enable pgcrypto / uuid-ossp; the JVM owns ID generation.
--
-- Soft delete pattern: rows have a nullable deleted_at column. A nightly
-- maintenance job hard-deletes rows where deleted_at < NOW() - 30 days.
-- ============================================================================

CREATE TABLE users (
    id              UUID         PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(72)  NOT NULL,    -- BCrypt cost 12 = 60 chars; pad to 72
    display_name    VARCHAR(80),
    plan            VARCHAR(16)  NOT NULL DEFAULT 'STARTER',  -- STARTER | PRO | UNLIMITED
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT chk_users_plan CHECK (plan IN ('STARTER', 'PRO', 'UNLIMITED'))
);

-- Email uniqueness applies only to live (non-deleted) accounts so a user can
-- delete + re-register with the same email after the 30-day soft-delete window.
CREATE UNIQUE INDEX idx_users_email_active
    ON users (LOWER(email))
    WHERE deleted_at IS NULL;

CREATE INDEX idx_users_deleted_at
    ON users (deleted_at)
    WHERE deleted_at IS NOT NULL;


CREATE TABLE sessions (
    id                  UUID         PRIMARY KEY,
    user_id             UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    refresh_token_hash  CHAR(64)     NOT NULL,  -- hex of SHA-256(refresh_token)
    user_agent          VARCHAR(500),
    ip_address          INET,
    expires_at          TIMESTAMPTZ  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_used_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    revoked_at          TIMESTAMPTZ
);

CREATE INDEX idx_sessions_user
    ON sessions (user_id);

-- Lookup by refresh-token-hash on every refresh request — must be fast.
CREATE UNIQUE INDEX idx_sessions_refresh_token_hash
    ON sessions (refresh_token_hash);

-- Active-sessions-per-user query (e.g., "list my devices"):
CREATE INDEX idx_sessions_active
    ON sessions (user_id)
    WHERE revoked_at IS NULL;
