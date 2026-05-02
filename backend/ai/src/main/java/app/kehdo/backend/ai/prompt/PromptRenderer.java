package app.kehdo.backend.ai.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders externalized prompt templates from
 * {@code src/main/resources/prompts/*.mustache} per {@code backend/CLAUDE.md}
 * AI rule 2.
 *
 * <p>Phase 4 PR 3 ships a deliberately minimal {@code {{var}}} substitution
 * — enough for the variables our templates currently need (parsed
 * conversation, tone, count, voice fingerprint, contact profile). Phase 4
 * PR 10 (3-layer prompt injection) swaps in real Mustache via JMustache
 * when sections / loops become useful. The {@code .mustache} extension is
 * already in place so consumers don't need to change.</p>
 *
 * <p>Templates are loaded once per JVM and cached. Variable lookup is
 * strict — a {@code {{var}}} with no value in the context throws to surface
 * template/code drift early.</p>
 */
@Component
public class PromptRenderer {

    private static final Pattern VAR = Pattern.compile("\\{\\{\\s*([a-zA-Z_][a-zA-Z0-9_.]*)\\s*}}");

    private final Map<String, String> templateCache = new LinkedHashMap<>();

    public String render(String templateName, Map<String, String> context) {
        String template = loadTemplate(templateName);
        Matcher m = VAR.matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            if (!context.containsKey(key)) {
                throw new IllegalStateException(
                        "Prompt template '" + templateName + "' references unknown variable: " + key);
            }
            m.appendReplacement(out, Matcher.quoteReplacement(context.get(key)));
        }
        m.appendTail(out);
        return out.toString();
    }

    private String loadTemplate(String name) {
        return templateCache.computeIfAbsent(name, n -> {
            String path = "prompts/" + n + ".mustache";
            try (var stream = new ClassPathResource(path).getInputStream()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Prompt template not found on classpath: " + path, e);
            }
        });
    }
}
