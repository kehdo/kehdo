package app.kehdo.backend.ai.llm;

/**
 * Single, mandatory entry point for every LLM call in the system.
 *
 * <p>Per {@code backend/CLAUDE.md} AI rule 1, no other module may import
 * Vertex AI / OpenAI SDKs directly — they go through this interface, behind
 * which sits the Resilience4j circuit breaker and the
 * primary→failover routing (Vertex AI Gemini 2.0 Flash primary,
 * OpenAI gpt-4o-mini failover, per ADR 0006).</p>
 *
 * <p>Implementations live in {@code :ai/llm/}:
 * <ul>
 *   <li>{@code StubLlmService} — Phase 4 PR 3 — canned replies, no network</li>
 *   <li>{@code VertexAiLlmService} — Phase 4 PR 7 — primary</li>
 *   <li>{@code OpenAiLlmService} — Phase 4 PR 8 — failover</li>
 *   <li>{@code FailoverLlmService} — Phase 4 PR 8 — composes the two above</li>
 * </ul>
 */
public interface LlmService {

    LlmResponse generate(LlmRequest request);
}
