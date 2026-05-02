package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls a {@code List<String>} of replies out of whatever Gemini returns.
 *
 * <p>With {@code responseMimeType=application/json} the model is supposed
 * to emit a clean JSON array. In practice it usually does, but we still
 * defend against:
 * <ul>
 *   <li>Markdown fences ({@code ```json ... ```}) when an older model
 *       version slips them in</li>
 *   <li>Wrapped objects like {@code {"replies": [...]}} when the model
 *       forgets the array-only instruction</li>
 *   <li>Extra prose either side of the array (we slice from the first
 *       {@code [} to the last {@code ]})</li>
 * </ul>
 *
 * <p>If none of those produce a parseable {@code List<String>}, we throw
 * — the caller's circuit breaker counts it as a failure, which is the
 * right signal: a model that consistently returns garbage shouldn't keep
 * burning quota.</p>
 */
public final class VertexAiResponseParser {

    private static final Pattern FENCE = Pattern.compile("(?s)```(?:json)?\\s*(.*?)\\s*```");
    private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {};

    private final ObjectMapper json;

    public VertexAiResponseParser(ObjectMapper json) {
        this.json = json;
    }

    public List<String> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new VertexAiResponseException("Vertex AI returned empty response");
        }
        String stripped = stripMarkdownFence(raw).trim();

        // 1) Direct: the whole payload is a JSON array.
        List<String> direct = tryParseArray(stripped);
        if (direct != null) return direct;

        // 2) Wrapped: {"replies": [...]} or similar — pull the first array we find.
        List<String> wrapped = tryParseWrappedArray(stripped);
        if (wrapped != null) return wrapped;

        // 3) Best-effort slice: substring between the first '[' and last ']'.
        int start = stripped.indexOf('[');
        int end = stripped.lastIndexOf(']');
        if (start != -1 && end > start) {
            List<String> sliced = tryParseArray(stripped.substring(start, end + 1));
            if (sliced != null) return sliced;
        }

        throw new VertexAiResponseException("Vertex AI response was not a parseable list of strings: "
                + raw.substring(0, Math.min(raw.length(), 200)));
    }

    private static String stripMarkdownFence(String raw) {
        Matcher m = FENCE.matcher(raw);
        return m.find() ? m.group(1) : raw;
    }

    private List<String> tryParseArray(String candidate) {
        try {
            return json.readValue(candidate, LIST_OF_STRING);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> tryParseWrappedArray(String candidate) {
        try {
            JsonNode root = json.readTree(candidate);
            if (root.isArray()) {
                // already handled by tryParseArray, but defensive
                return json.convertValue(root, LIST_OF_STRING);
            }
            if (root.isObject()) {
                for (JsonNode child : root) {
                    if (child.isArray()) {
                        return json.convertValue(child, LIST_OF_STRING);
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through to next strategy
        }
        return null;
    }
}
