package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiLlmServiceTest {

    private OpenAiClient client;
    private OpenAiLlmService service;

    @BeforeEach
    void setUp() {
        client = mock(OpenAiClient.class);
        service = new OpenAiLlmService(client, new ObjectMapper(), "gpt-4o-mini");
    }

    @Test
    void sends_json_object_request_and_unwraps_replies_array() {
        // OpenAI's json_object mode constrains output to a JSON object —
        // the model wraps as {"replies": [...]}; the shared parser unwraps.
        when(client.complete(org.mockito.ArgumentMatchers.any())).thenReturn(
                "{\"replies\": [\"hi there\", \"all good\", \"sounds fun\"]}");

        LlmResponse response = service.generate(new LlmRequest("any prompt", "WARM", 3));

        ArgumentCaptor<OpenAiClient.ChatCompletionRequest> captor =
                ArgumentCaptor.forClass(OpenAiClient.ChatCompletionRequest.class);
        verify(client).complete(captor.capture());
        OpenAiClient.ChatCompletionRequest sent = captor.getValue();
        assertThat(sent.model()).isEqualTo("gpt-4o-mini");
        assertThat(sent.responseFormat().type()).isEqualTo("json_object");
        assertThat(sent.messages()).hasSize(1);
        assertThat(sent.messages().get(0).role()).isEqualTo("user");
        assertThat(sent.messages().get(0).content()).isEqualTo("any prompt");

        assertThat(response.replies())
                .extracting(LlmReply::rank, LlmReply::text)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple(1, "hi there"),
                        org.assertj.core.api.Assertions.tuple(2, "all good"),
                        org.assertj.core.api.Assertions.tuple(3, "sounds fun"));
        assertThat(response.modelUsed()).isEqualTo("openai/gpt-4o-mini");
    }

    @Test
    void caps_replies_at_requested_count() {
        when(client.complete(org.mockito.ArgumentMatchers.any())).thenReturn(
                "{\"replies\": [\"a\", \"b\", \"c\", \"d\", \"e\"]}");

        LlmResponse response = service.generate(new LlmRequest("any prompt", "WARM", 2));

        assertThat(response.replies()).hasSize(2);
        assertThat(response.replies()).extracting(LlmReply::text).containsExactly("a", "b");
    }

    @Test
    void propagates_openai_call_exception_for_breaker() {
        when(client.complete(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new OpenAiCallException("upstream broken"));

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(OpenAiCallException.class)
                .hasMessageContaining("upstream broken");
    }

    @Test
    void empty_array_response_throws() {
        when(client.complete(org.mockito.ArgumentMatchers.any()))
                .thenReturn("{\"replies\": []}");

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(OpenAiCallException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void garbled_response_throws_via_parser() {
        when(client.complete(org.mockito.ArgumentMatchers.any())).thenReturn("the model went rogue");

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 2)))
                .isInstanceOf(VertexAiResponseException.class);
    }
}
