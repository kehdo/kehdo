package app.kehdo.backend.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptRendererTest {

    private final PromptRenderer renderer = new PromptRenderer();

    @Test
    void substitutes_variables_in_the_built_in_generate_replies_template() {
        String out = renderer.render("generate-replies", Map.of(
                "tone", "WARM",
                "conversation", "THEM: hi\nME: hey",
                "count", "4"
        ));

        assertThat(out)
                .contains("Tone for this batch: WARM")
                .contains("THEM: hi\nME: hey")
                .contains("Produce 4 ranked reply options")
                .doesNotContain("{{");
    }

    @Test
    void unknown_variable_throws_so_template_drift_surfaces_immediately() {
        assertThatThrownBy(() -> renderer.render("generate-replies", Map.of(
                // missing "count"
                "tone", "WARM",
                "conversation", "THEM: hi"
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("count");
    }

    @Test
    void missing_template_file_throws_with_classpath_path() {
        assertThatThrownBy(() -> renderer.render("does-not-exist", Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prompts/does-not-exist.mustache");
    }
}
