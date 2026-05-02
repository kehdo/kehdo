package app.kehdo.backend.ai.llm;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the documented failover triggers behave as advertised:
 * primary call succeeds → use primary; primary throws breaker-open or
 * unusable-response → fall through to fallback; both fail → propagate.
 */
class FailoverLlmServiceTest {

    private VertexAiLlmService primary;
    private OpenAiLlmService fallback;
    private FailoverLlmService service;

    @BeforeEach
    void setUp() {
        primary = mock(VertexAiLlmService.class);
        fallback = mock(OpenAiLlmService.class);
        service = new FailoverLlmService(primary, fallback);
    }

    @Test
    void uses_primary_when_it_succeeds() {
        LlmResponse expected = ok("vertex-ai/gemini-2.0-flash", "from primary");
        when(primary.generate(any())).thenReturn(expected);

        LlmResponse out = service.generate(new LlmRequest("any prompt", "WARM", 1));

        assertThat(out).isSameAs(expected);
        verify(fallback, times(0)).generate(any());
    }

    @Test
    void falls_over_when_primary_breaker_is_open() {
        CircuitBreaker openBreaker = CircuitBreaker.ofDefaults("llm-vertex");
        openBreaker.transitionToOpenState();
        when(primary.generate(any())).thenThrow(CallNotPermittedException.createCallNotPermittedException(openBreaker));
        LlmResponse fallbackResponse = ok("openai/gpt-4o-mini", "from fallback");
        when(fallback.generate(any())).thenReturn(fallbackResponse);

        LlmResponse out = service.generate(new LlmRequest("any prompt", "WARM", 1));

        assertThat(out).isSameAs(fallbackResponse);
        verify(fallback).generate(any());
    }

    @Test
    void falls_over_when_primary_returns_unusable_response() {
        when(primary.generate(any())).thenThrow(new VertexAiResponseException("malformed"));
        LlmResponse fallbackResponse = ok("openai/gpt-4o-mini", "from fallback");
        when(fallback.generate(any())).thenReturn(fallbackResponse);

        LlmResponse out = service.generate(new LlmRequest("any prompt", "WARM", 1));

        assertThat(out).isSameAs(fallbackResponse);
    }

    @Test
    void falls_over_on_any_unexpected_runtime_exception_from_primary() {
        when(primary.generate(any())).thenThrow(new RuntimeException("upstream broken"));
        LlmResponse fallbackResponse = ok("openai/gpt-4o-mini", "from fallback");
        when(fallback.generate(any())).thenReturn(fallbackResponse);

        LlmResponse out = service.generate(new LlmRequest("any prompt", "WARM", 1));

        assertThat(out).isSameAs(fallbackResponse);
    }

    @Test
    void propagates_when_fallback_also_fails() {
        when(primary.generate(any())).thenThrow(new VertexAiResponseException("primary failed"));
        when(fallback.generate(any())).thenThrow(new OpenAiCallException("fallback failed too"));

        assertThatThrownBy(() -> service.generate(new LlmRequest("any prompt", "WARM", 1)))
                .isInstanceOf(OpenAiCallException.class)
                .hasMessage("fallback failed too");
    }

    private static LlmResponse ok(String model, String text) {
        return new LlmResponse(List.of(new LlmReply(1, text)), model);
    }
}
