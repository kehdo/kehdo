package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage for {@link VertexAiLlmService}. Mocks
 * {@link GenerativeModel} directly so the test never reaches the network.
 * Resilience4j retry/circuit-breaker decorators are off in tests (no
 * Spring context) — those are exercised in integration runs.
 */
class VertexAiLlmServiceTest {

    private GenerativeModel model;
    private VertexAiLlmService service;

    @BeforeEach
    void setUp() {
        model = mock(GenerativeModel.class);
        service = new VertexAiLlmService(model, new ObjectMapper(), "gemini-2.0-flash");
    }

    @Test
    void returns_ranked_replies_with_vendor_prefixed_model_id() throws IOException {
        when(model.generateContent(anyString()))
                .thenReturn(responseWith("[\"hey there\", \"all good\", \"sounds fun\"]"));

        LlmResponse response = service.generate(new LlmRequest("any prompt", "WARM", 3));

        assertThat(response.replies())
                .extracting(LlmReply::rank, LlmReply::text)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple(1, "hey there"),
                        org.assertj.core.api.Assertions.tuple(2, "all good"),
                        org.assertj.core.api.Assertions.tuple(3, "sounds fun"));
        assertThat(response.modelUsed()).isEqualTo("vertex-ai/gemini-2.0-flash");
    }

    @Test
    void caps_replies_at_requested_count() throws IOException {
        when(model.generateContent(anyString()))
                .thenReturn(responseWith("[\"a\", \"b\", \"c\", \"d\", \"e\"]"));

        LlmResponse response = service.generate(new LlmRequest("any prompt", "WARM", 2));

        assertThat(response.replies()).hasSize(2);
        assertThat(response.replies()).extracting(LlmReply::text).containsExactly("a", "b");
    }

    @Test
    void wraps_io_exception_so_circuit_breaker_counts_it() throws IOException {
        when(model.generateContent(anyString())).thenThrow(new IOException("upstream broken"));

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(VertexAiResponseException.class)
                .hasMessageContaining("upstream broken");
    }

    @Test
    void empty_array_response_throws() throws IOException {
        when(model.generateContent(anyString())).thenReturn(responseWith("[]"));

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(VertexAiResponseException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void garbled_response_throws() throws IOException {
        when(model.generateContent(anyString())).thenReturn(responseWith("the model went rogue"));

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(VertexAiResponseException.class);
    }

    // ---- helpers -------------------------------------------------------

    private static GenerateContentResponse responseWith(String text) {
        return GenerateContentResponse.newBuilder()
                .addCandidates(Candidate.newBuilder()
                        .setContent(Content.newBuilder()
                                .addParts(Part.newBuilder().setText(text).build())
                                .build())
                        .build())
                .build();
    }
}
