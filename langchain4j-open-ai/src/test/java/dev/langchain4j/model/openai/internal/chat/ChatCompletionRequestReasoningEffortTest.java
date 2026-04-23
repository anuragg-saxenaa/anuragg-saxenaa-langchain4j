package dev.langchain4j.model.openai.internal.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.junit.jupiter.api.Test;

/**
 * Regression test for GitHub issue #4898.
 *
 * <p>OpenAI updated their API: reasoning effort changed from flat string
 * {@code reasoning_effort} to nested object {@code reasoning: {"effort": "..."}}.
 * This test verifies that ChatCompletionRequest serializes reasoning effort
 * as the new nested structure.
 *
 * @see <a href="https://github.com/langchain4j/langchain4j/issues/4898">Issue #4898</a>
 */
class ChatCompletionRequestReasoningEffortTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Test
    void reasoningEffort_should_serialize_as_nested_reasoning_object() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .reasoningEffort("low")
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then - should have nested "reasoning": {"effort": "low"} NOT "reasoning_effort": "low"
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).contains("\"reasoning\":{");
        assertThat(json).contains("\"effort\":\"low\"");
    }

    @Test
    void reasoningEffort_should_serialize_medium_effort_correctly() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .reasoningEffort("medium")
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).contains("\"reasoning\":{");
        assertThat(json).contains("\"effort\":\"medium\"");
    }

    @Test
    void reasoningEffort_should_serialize_high_effort_correctly() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .reasoningEffort("high")
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).contains("\"reasoning\":{");
        assertThat(json).contains("\"effort\":\"high\"");
    }

    @Test
    void null_reasoningEffort_should_not_include_reasoning_field() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then - no reasoning field at all when effort is null
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).doesNotContain("\"reasoning\"");
    }

    @Test
    void reasoning_can_be_set_directly_as_map() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .reasoning(java.util.Map.of("effort", "low"))
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).contains("\"reasoning\":{");
        assertThat(json).contains("\"effort\":\"low\"");
    }

    @Test
    void builder_backward_compatibility_reasoningEffort_returns_correct_value() {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .addUserMessage("Hello")
                .reasoningEffort("low")
                .build();

        // then - the reasoning map should contain the effort
        assertThat(request.reasoning()).isNotNull();
        assertThat(request.reasoning()).containsEntry("effort", "low");
    }

    @Test
    void full_request_with_reasoningEffort_should_serialize_correctly() throws JsonProcessingException {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-5.4-mini")
                .temperature(0.7)
                .maxTokens(100)
                .addUserMessage("Write a bash script")
                .reasoningEffort("low")
                .build();

        // when
        String json = OBJECT_MAPPER.writeValueAsString(request);

        // then
        assertThat(json).doesNotContain("reasoning_effort");
        assertThat(json).contains("\"model\":\"gpt-5.4-mini\"");
        assertThat(json).contains("\"temperature\":0.7");
        assertThat(json).contains("\"max_tokens\":100");
        assertThat(json).contains("\"reasoning\":{");
        assertThat(json).contains("\"effort\":\"low\"");
    }
}
