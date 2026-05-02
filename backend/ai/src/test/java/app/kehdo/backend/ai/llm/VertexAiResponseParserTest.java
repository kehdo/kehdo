package app.kehdo.backend.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VertexAiResponseParserTest {

    private final VertexAiResponseParser parser = new VertexAiResponseParser(new ObjectMapper());

    @Test
    void parses_clean_json_array() {
        List<String> out = parser.parse("[\"hi there\", \"all good\", \"sounds fun\"]");

        assertThat(out).containsExactly("hi there", "all good", "sounds fun");
    }

    @Test
    void strips_markdown_json_fence() {
        String raw = """
                ```json
                ["one", "two"]
                ```""";

        assertThat(parser.parse(raw)).containsExactly("one", "two");
    }

    @Test
    void strips_unlabeled_markdown_fence() {
        String raw = """
                ```
                ["one"]
                ```""";

        assertThat(parser.parse(raw)).containsExactly("one");
    }

    @Test
    void handles_wrapped_object_with_array_field() {
        String raw = "{\"replies\": [\"a\", \"b\"]}";

        assertThat(parser.parse(raw)).containsExactly("a", "b");
    }

    @Test
    void slices_array_out_of_extra_prose() {
        String raw = "Sure! Here are the options: [\"opt1\", \"opt2\"] — happy replying!";

        assertThat(parser.parse(raw)).containsExactly("opt1", "opt2");
    }

    @Test
    void blank_response_throws() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(VertexAiResponseException.class);
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(VertexAiResponseException.class);
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(VertexAiResponseException.class);
    }

    @Test
    void unparseable_response_throws() {
        assertThatThrownBy(() -> parser.parse("the model went rogue"))
                .isInstanceOf(VertexAiResponseException.class);
        assertThatThrownBy(() -> parser.parse("[broken json"))
                .isInstanceOf(VertexAiResponseException.class);
    }
}
