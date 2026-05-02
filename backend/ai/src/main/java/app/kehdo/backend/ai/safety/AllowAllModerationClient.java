package app.kehdo.backend.ai.safety;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Placeholder moderation. Allows every candidate. Real moderation lands in
 * Phase 4 PR 11 — flip {@code kehdo.ai.moderation.provider=openai} when
 * that adapter ships. Never deploy to production with the stub active.
 */
@Service
@ConditionalOnProperty(name = "kehdo.ai.moderation.provider", havingValue = "stub", matchIfMissing = true)
public class AllowAllModerationClient implements ModerationClient {

    @Override
    public ModerationVerdict check(String candidateText) {
        return ModerationVerdict.allow();
    }
}
