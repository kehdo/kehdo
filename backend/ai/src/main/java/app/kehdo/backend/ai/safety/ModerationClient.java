package app.kehdo.backend.ai.safety;

/**
 * Mandatory safety gate on every generated reply per
 * {@code backend/CLAUDE.md} AI rule 5.
 *
 * <p>The orchestrator (Phase 4 PR 4 onward) calls {@link #check} on each
 * candidate reply BEFORE persisting it. Blocked replies are dropped
 * silently from the response if at least one sibling passes; if the
 * whole batch is blocked the controller surfaces
 * {@code CONTENT_BLOCKED} (HTTP 422) per the contract.</p>
 *
 * <p>Implementations live in {@code :ai/safety/}:
 * <ul>
 *   <li>{@code AllowAllModerationClient} — Phase 4 PR 3 — no-op stub</li>
 *   <li>{@code OpenAiModerationClient} — Phase 4 PR 11 — real moderation</li>
 * </ul>
 */
public interface ModerationClient {

    ModerationVerdict check(String candidateText);
}
