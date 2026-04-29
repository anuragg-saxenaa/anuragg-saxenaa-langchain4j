package dev.langchain4j.model.openai.internal.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ChatCompletionRequestSerializationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reasoningEffort_should_serialize_as_nested_json() throws Exception {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .addSystemMessage("You are helpful")
                .addUserMessage("Hello")
                .reasoningEffort("low")
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);

        // then
        assertThat(json).contains("\"reasoning\":{\"effort\":\"low\"}");
        assertThat(json).doesNotContain("reasoning_effort");
    }

    @Test
    void reasoning_with_object_should_serialize_as_nested_json() throws Exception {
        // given
        Reasoning reasoning = new Reasoning("high");
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .addUserMessage("Hello")
                .reasoning(reasoning)
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);

        // then
        assertThat(json).contains("\"reasoning\":{\"effort\":\"high\"}");
        assertThat(json).doesNotContain("reasoning_effort");
    }

    @Test
    void reasoningEffort_null_should_not_appear_in_json() throws Exception {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .addUserMessage("Hello")
                .build();

        // when
        String json = objectMapper.writeValueAsString(request);

        // then
        assertThat(json).doesNotContain("reasoning");
        assertThat(json).doesNotContain("reasoning_effort");
    }
}