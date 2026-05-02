-- ============================================================================
-- V2 — Phase 4 schema: conversations + replies
-- ============================================================================
-- A conversation is the unit a user uploads (one screenshot = one
-- conversation). Status lifecycle:
--
--   PENDING_UPLOAD → client has the presigned URL but hasn't PUT yet
--   PROCESSING     → screenshot received, OCR / speaker / LLM in flight
--   READY          → parsed_messages populated, replies generated
--   FAILED         → terminal failure (OCR couldn't read, LLM both
--                    primary + failover errored, content blocked, etc.)
--
-- parsed_messages is JSONB so we can read+write the whole array in one
-- shot from the OCR pipeline without a join. We don't query inside it.
--
-- replies are a flat row-per-suggestion so /replies/{id}/refine has an
-- O(1) lookup without parsing JSON.
--
-- Soft delete: conversations.deleted_at; replies cascade via FK.
-- ============================================================================

CREATE TABLE conversations (
    id                          UUID          PRIMARY KEY,
    user_id                     UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status                      VARCHAR(16)   NOT NULL DEFAULT 'PENDING_UPLOAD',
    screenshot_object_key       VARCHAR(255),
    parsed_messages             JSONB,
    ocr_completed_at            TIMESTAMPTZ,
    last_generation_model       VARCHAR(64),
    last_generation_at          TIMESTAMPTZ,
    failure_reason              VARCHAR(64),
    created_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at                  TIMESTAMPTZ,

    CONSTRAINT chk_conversations_status
        CHECK (status IN ('PENDING_UPLOAD', 'PROCESSING', 'READY', 'FAILED'))
);

-- Active-conversations-by-user query (powers GET /conversations).
-- Indexed on (user_id, created_at DESC) for efficient pagination.
CREATE INDEX idx_conversations_user_active
    ON conversations (user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- Lookup of a single live conversation by id (most reads).
CREATE INDEX idx_conversations_active_id
    ON conversations (id)
    WHERE deleted_at IS NULL;

-- Soft-delete cleanup job needs an index to find rows older than 30 days.
CREATE INDEX idx_conversations_deleted_at
    ON conversations (deleted_at)
    WHERE deleted_at IS NOT NULL;


CREATE TABLE replies (
    id                  UUID          PRIMARY KEY,
    conversation_id     UUID          NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    rank                INTEGER       NOT NULL,
    text                TEXT          NOT NULL,
    tone_code           VARCHAR(32)   NOT NULL,
    model_used          VARCHAR(64)   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_replies_rank CHECK (rank BETWEEN 1 AND 5)
);

-- /replies/{id}/refine reads by id only — primary key serves that.
-- /conversations/{id} returns the latest generation's replies; this
-- index makes that lookup ordered.
CREATE INDEX idx_replies_conversation_rank
    ON replies (conversation_id, rank);

-- Phase 4 quota enforcement (Phase 4 PR 14) counts replies-per-day per
-- user. Joining replies → conversations → users is fine for the volumes
-- a beta sees; if it ever isn't, we denormalize user_id onto replies
-- and add an index then.
