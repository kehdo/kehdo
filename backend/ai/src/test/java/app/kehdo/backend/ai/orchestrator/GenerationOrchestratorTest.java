package app.kehdo.backend.ai.orchestrator;

import app.kehdo.backend.ai.budget.TokenBudgeter;
import app.kehdo.backend.ai.llm.LlmReply;
import app.kehdo.backend.ai.llm.LlmRequest;
import app.kehdo.backend.ai.llm.LlmResponse;
import app.kehdo.backend.ai.llm.LlmService;
import app.kehdo.backend.ai.ocr.OcrResult;
import app.kehdo.backend.ai.ocr.OcrService;
import app.kehdo.backend.ai.prompt.PromptRenderer;
import app.kehdo.backend.ai.safety.ModerationClient;
import app.kehdo.backend.ai.safety.ModerationVerdict;
import app.kehdo.backend.ai.speaker.SpeakerAttributor;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine;
import app.kehdo.backend.ai.speaker.SpeakerAttributor.AttributedLine.Speaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenerationOrchestratorTest {

    private OcrService ocr;
    private SpeakerAttributor speaker;
    private LlmService llm;
    private ModerationClient moderation;
    private GenerationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        ocr = mock(OcrService.class);
        speaker = mock(SpeakerAttributor.class);
        llm = mock(LlmService.class);
        moderation = mock(ModerationClient.class);
        orchestrator = new GenerationOrchestrator(
                ocr,
                speaker,
                new PromptRenderer(),
                new TokenBudgeter(3000),
                llm,
                moderation);
    }

    @Test
    void happy_path_runs_each_stage_in_order_and_returns_approved_replies() {
        when(ocr.read("key")).thenReturn(new OcrResult(List.of("hi", "hey"), 0.95, null));
        List<AttributedLine> attributed = List.of(
                new AttributedLine(Speaker.THEM, "hi", 0.9),
                new AttributedLine(Speaker.ME, "hey", 0.9));
        when(speaker.attribute(List.of("hi", "hey"))).thenReturn(attributed);
        when(llm.generate(any(LlmRequest.class))).thenReturn(new LlmResponse(
                List.of(new LlmReply(1, "Sounds good"), new LlmReply(2, "All set")),
                "stub/canned-v1"));
        when(moderation.check(any())).thenReturn(ModerationVerdict.allow());

        GenerationOutput out = orchestrator.run(new GenerationRequest("key", "WARM", 2));

        assertThat(out.attributedMessages()).isEqualTo(attributed);
        assertThat(out.replies()).extracting(LlmReply::text).containsExactly("Sounds good", "All set");
        assertThat(out.modelUsed()).isEqualTo("stub/canned-v1");
    }

    @Test
    void filters_out_replies_blocked_by_moderation() {
        when(ocr.read("key")).thenReturn(new OcrResult(List.of("hi"), 0.95, null));
        when(speaker.attribute(any())).thenReturn(
                List.of(new AttributedLine(Speaker.THEM, "hi", 0.9)));
        when(llm.generate(any())).thenReturn(new LlmResponse(
                List.of(new LlmReply(1, "ok"), new LlmReply(2, "bad")),
                "stub/canned-v1"));
        when(moderation.check("ok")).thenReturn(ModerationVerdict.allow());
        when(moderation.check("bad")).thenReturn(ModerationVerdict.block("HATE", 0.9));

        GenerationOutput out = orchestrator.run(new GenerationRequest("key", "WARM", 2));

        assertThat(out.replies()).hasSize(1);
        assertThat(out.replies().get(0).text()).isEqualTo("ok");
    }

    @Test
    void throws_content_blocked_when_every_reply_is_blocked() {
        when(ocr.read("key")).thenReturn(new OcrResult(List.of("hi"), 0.95, null));
        when(speaker.attribute(any())).thenReturn(
                List.of(new AttributedLine(Speaker.THEM, "hi", 0.9)));
        when(llm.generate(any())).thenReturn(new LlmResponse(
                List.of(new LlmReply(1, "bad1"), new LlmReply(2, "bad2")),
                "stub/canned-v1"));
        when(moderation.check(any())).thenReturn(ModerationVerdict.block("HATE", 0.9));

        assertThatThrownBy(() -> orchestrator.run(new GenerationRequest("key", "WARM", 2)))
                .isInstanceOf(ContentBlockedException.class);
    }
}
