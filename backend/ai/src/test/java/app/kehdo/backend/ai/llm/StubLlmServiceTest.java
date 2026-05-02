package app.kehdo.backend.ai.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StubLlmServiceTest {

    private final StubLlmService service = new StubLlmService();

    @Test
    void returns_count_replies_ranked_starting_at_one() {
        LlmResponse r = service.generate(new LlmRequest("any prompt", "WARM", 3));

        assertThat(r.replies()).hasSize(3);
        assertThat(r.replies()).extracting(LlmReply::rank).containsExactly(1, 2, 3);
        assertThat(r.modelUsed()).isEqualTo(StubLlmService.MODEL_ID);
    }

    @Test
    void caps_count_at_canned_pool_size() {
        // Stub has 5 canned bodies; asking for 5 returns 5.
        LlmResponse r = service.generate(new LlmRequest("any prompt", "WARM", 5));
        assertThat(r.replies()).hasSize(5);
    }
}
