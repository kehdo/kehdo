package app.kehdo.backend.ai.orchestrator;

import app.kehdo.backend.ai.budget.TokenBudgeter;
import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.llm.LlmRequest;
import app.kehdo.backend.ai.llm.LlmResponse;
import app.kehdo.backend.ai.llm.LlmService;
import app.kehdo.backend.ai.prompt.PromptRenderer;
import app.kehdo.backend.ai.safety.ModerationClient;
import app.kehdo.backend.ai.safety.ModerationVerdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefineOrchestratorTest {

    private LlmService llm;
    private ModerationClient moderation;
    private RefineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        llm = mock(LlmService.class);
        moderation = mock(ModerationClient.class);
        orchestrator = new RefineOrchestrator(
                new PromptRenderer(),
                new TokenBudgeter(3000),
                llm,
                moderation);
    }

    @Test
    void renders_prompt_with_three_variables_and_returns_refined_text() {
        when(llm.generate(any())).thenReturn(new LlmResponse(
                List.of(new LlmReply(1, "Got you — chill plan it is 🙂")),
                "vertex-ai/gemini-2.0-flash"));
        when(moderation.check(any())).thenReturn(ModerationVerdict.allow());

        var output = orchestrator.refine(new RefineOrchestrator.RefineInput(
                "Sure! Tonight at 7?",
                "WARM",
                "shorter, add a smiley"));

        assertThat(output.text()).isEqualTo("Got you — chill plan it is 🙂");
        assertThat(output.modelUsed()).isEqualTo("vertex-ai/gemini-2.0-flash");

        // Verify the LLM saw a prompt that contains the three substitution slots.
        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llm).generate(captor.capture());
        String prompt = captor.getValue().prompt();
        assertThat(prompt)
                .contains("Sure! Tonight at 7?")
                .contains("Tone: WARM")
                .contains("shorter, add a smiley");
        assertThat(captor.getValue().count()).isEqualTo(1);
    }

    @Test
    void empty_llm_response_throws_content_blocked() {
        // Cannot construct a 0-element LlmResponse (record validates) — instead
        // simulate "ranked-but-blocked-everything" by returning one reply that
        // moderation will block.
        when(llm.generate(any())).thenReturn(new LlmResponse(
                List.of(new LlmReply(1, "blocked text")),
                "vertex-ai/gemini-2.0-flash"));
        when(moderation.check(any())).thenReturn(ModerationVerdict.block("HATE", 0.9));

        assertThatThrownBy(() -> orchestrator.refine(new RefineOrchestrator.RefineInput(
                "original",
                "WARM",
                "shorter")))
                .isInstanceOf(ContentBlockedException.class);
    }

    @Test
    void rejects_blank_inputs() {
        assertThatThrownBy(() -> new RefineOrchestrator.RefineInput("", "WARM", "shorter"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RefineOrchestrator.RefineInput("orig", "", "shorter"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RefineOrchestrator.RefineInput("orig", "WARM", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
