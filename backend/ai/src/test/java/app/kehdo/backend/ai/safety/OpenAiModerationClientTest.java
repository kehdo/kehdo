package app.kehdo.backend.ai.safety;

import app.kehdo.backend.ai.llm.OpenAiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Direct method-level tests. The Resilience4j fallback path
 * ({@code checkFallback} → fail open) only fires through Spring AOP,
 * so it's not covered here — the behavior is documented on the method
 * and exercised in production via the {@code moderation} circuit
 * breaker.
 */
class OpenAiModerationClientTest {

    private OpenAiClient client;
    private OpenAiModerationClient moderation;

    @BeforeEach
    void setUp() {
        client = mock(OpenAiClient.class);
        moderation = new OpenAiModerationClient(client, "omni-moderation-latest");
    }

    @Test
    void allows_when_not_flagged() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                false,
                Map.of("hate", false, "violence", false),
                Map.of("hate", 0.001, "violence", 0.002)));

        ModerationVerdict verdict = moderation.check("hello there");

        assertThat(verdict.allowed()).isTrue();
        assertThat(verdict.category()).isNull();
        assertThat(verdict.providerScore()).isNull();
    }

    @Test
    void sends_configured_model_and_input_to_openai() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                false, Map.of(), Map.of()));

        moderation.check("hello there");

        ArgumentCaptor<OpenAiClient.ModerationRequest> captor =
                ArgumentCaptor.forClass(OpenAiClient.ModerationRequest.class);
        verify(client).moderate(captor.capture());
        assertThat(captor.getValue().model()).isEqualTo("omni-moderation-latest");
        assertThat(captor.getValue().input()).isEqualTo("hello there");
    }

    @Test
    void blocks_with_canonical_hate_category_when_only_hate_flagged() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("hate", true, "violence", false),
                Map.of("hate", 0.92, "violence", 0.04)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("HATE");
        assertThat(verdict.providerScore()).isEqualTo(0.92);
    }

    @Test
    void picks_highest_scoring_flagged_category_when_multiple_fire() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of(
                        "hate", true,
                        "violence", true,
                        "self-harm", true),
                Map.of(
                        "hate", 0.45,
                        "violence", 0.88,
                        "self-harm", 0.12)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("VIOLENCE");
        assertThat(verdict.providerScore()).isEqualTo(0.88);
    }

    @Test
    void maps_sexual_minors_to_sexual_minors_bucket() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("sexual/minors", true),
                Map.of("sexual/minors", 0.99)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("SEXUAL_MINORS");
        assertThat(verdict.providerScore()).isEqualTo(0.99);
    }

    @Test
    void maps_self_harm_subcategories_to_self_harm_bucket() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("self-harm/intent", true),
                Map.of("self-harm/intent", 0.71)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.category()).isEqualTo("SELF_HARM");
    }

    @Test
    void maps_harassment_to_hate_bucket() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("harassment/threatening", true),
                Map.of("harassment/threatening", 0.66)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.category()).isEqualTo("HATE");
    }

    @Test
    void maps_violence_graphic_to_violence_bucket() {
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("violence/graphic", true),
                Map.of("violence/graphic", 0.55)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.category()).isEqualTo("VIOLENCE");
    }

    @Test
    void maps_adult_sexual_to_other_bucket() {
        // Plain "sexual" (not minors) is still blockable, but doesn't get
        // the SEXUAL_MINORS slot — that's reserved for the special case.
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("sexual", true),
                Map.of("sexual", 0.81)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("OTHER");
        assertThat(verdict.providerScore()).isEqualTo(0.81);
    }

    @Test
    void unknown_category_falls_back_to_other() {
        // Future OpenAI categories shouldn't crash us.
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("future-category", true),
                Map.of("future-category", 0.50)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.category()).isEqualTo("OTHER");
        assertThat(verdict.providerScore()).isEqualTo(0.50);
    }

    @Test
    void flagged_with_no_categories_map_blocks_as_other() {
        // Defensive: provider self-contradicts (flagged=true, no map).
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true, null, null));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("OTHER");
        assertThat(verdict.providerScore()).isNull();
    }

    @Test
    void flagged_with_all_false_entries_blocks_as_other() {
        // Another self-contradiction: flagged=true but no entry is true.
        when(client.moderate(any())).thenReturn(new OpenAiClient.ModerationResult(
                true,
                Map.of("hate", false, "violence", false),
                Map.of("hate", 0.01, "violence", 0.02)));

        ModerationVerdict verdict = moderation.check("blocked text");

        assertThat(verdict.allowed()).isFalse();
        assertThat(verdict.category()).isEqualTo("OTHER");
    }
}
