package dev.langchain4j.model.ollama;

import static dev.langchain4j.model.ollama.OllamaJsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.UserMessage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InternalOllamaHelperTest {

    @Test
    void toToolExecutionRequests_mapsToolCalls() {
        ToolCall toolCall = ToolCall.builder()
                .id("tool-1")
                .function(FunctionCall.builder()
                        .name("lookupWeather")
                        .arguments(Map.of("city", "Shanghai"))
                        .build())
                .build();

        List<ToolExecutionRequest> result = InternalOllamaHelper.toToolExecutionRequests(List.of(toolCall));

        assertThat(result)
                .containsExactly(ToolExecutionRequest.builder()
                        .id("tool-1")
                        .name("lookupWeather")
                        .arguments("{\"city\":\"Shanghai\"}")
                        .build());
    }

    @Test
    void toToolExecutionRequests_handlesEmptyToolCalls() {
        List<ToolExecutionRequest> result = InternalOllamaHelper.toToolExecutionRequests(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void toOllamaChatRequest_includesNumThreadInOptions() {
        OllamaChatRequestParameters parameters = OllamaChatRequestParameters.builder()
                .modelName("llama3")
                .numThread(4)
                .build();

        dev.langchain4j.model.chat.request.ChatRequest chatRequest =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(List.of(UserMessage.from("Hello")))
                        .parameters(parameters)
                        .build();

        OllamaChatRequest ollamaRequest = InternalOllamaHelper.toOllamaChatRequest(chatRequest, false);
        String json = toJson(ollamaRequest);

        assertThat(json).contains("num_thread");
        assertThat(ollamaRequest.getOptions().getNumThread()).isEqualTo(4);
    }

    @Test
    void toOllamaChatRequest_omitsNumThreadWhenNull() {
        OllamaChatRequestParameters parameters = OllamaChatRequestParameters.builder()
                .modelName("llama3")
                .build();

        dev.langchain4j.model.chat.request.ChatRequest chatRequest =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(List.of(UserMessage.from("Hello")))
                        .parameters(parameters)
                        .build();

        OllamaChatRequest ollamaRequest = InternalOllamaHelper.toOllamaChatRequest(chatRequest, false);
        String json = toJson(ollamaRequest);

        assertThat(json).doesNotContain("num_thread");
        assertThat(ollamaRequest.getOptions().getNumThread()).isNull();
    }

    @Test
    void ollamaChatRequestParameters_numThread_roundTrip() {
        OllamaChatRequestParameters original = OllamaChatRequestParameters.builder()
                .modelName("llama3")
                .numThread(8)
                .build();

        OllamaChatRequestParameters overridden = original.overrideWith(
                OllamaChatRequestParameters.builder().numThread(4).build());

        assertThat(overridden.numThread()).isEqualTo(4);
    }
}