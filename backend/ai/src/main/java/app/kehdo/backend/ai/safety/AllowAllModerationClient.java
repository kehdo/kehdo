package app.kehdo.backend.ai.safety;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Phase 4 PR 3 placeholder. Allows every candidate. The real moderation
 * gate (Phase 4 PR 11) replaces this — never ship to production with
 * this bean active.
 */
@Service
@Profile({"stub-llm", "test", "default"})
public class AllowAllModerationClient implements ModerationClient {

    @Override
    public ModerationVerdict check(String candidateText) {
        return ModerationVerdict.allow();
    }
}
