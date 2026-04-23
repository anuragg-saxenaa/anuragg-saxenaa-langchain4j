package dev.langchain4j.model.openai.internal;

import static dev.langchain4j.model.openai.internal.OpenAiUtils.aiMessageFrom;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.toOpenAiToolChoice;
import static dev.langchain4j.model.openai.internal.chat.ToolType.FUNCTION;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.openai.internal.chat.AssistantMessage;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionChoice;
import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;
import dev.langchain4j.model.openai.internal.chat.FunctionCall;
import dev.langchain4j.model.openai.internal.chat.ToolCall;
import dev.langchain4j.model.openai.internal.chat.ToolChoiceMode;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OpenAiUtilsTest {

    @Test
    void should_return_ai_message_with_text_when_no_functions_and_tool_calls_are_present() {

        // given
        String messageContent = "hello";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .content(messageContent)
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.text()).contains(messageContent);
        assertThat(aiMessage.toolExecutionRequests()).isEmpty();
    }

    @Test
    void should_return_ai_message_with_toolExecutionRequests_when_function_is_present() {

        // given
        String functionName = "current_time";
        String functionArguments = "{}";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .functionCall(FunctionCall.builder()
                                        .name(functionName)
                                        .arguments(functionArguments)
                                        .build())
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.toolExecutionRequests())
                .containsExactly(ToolExecutionRequest.builder()
                        .name(functionName)
                        .arguments(functionArguments)
                        .build());
    }

    @Test
    void should_return_ai_message_with_toolExecutionRequests_when_tool_calls_are_present() {

        // given
        String functionName = "current_time";
        String functionArguments = "{}";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .toolCalls(ToolCall.builder()
                                        .type(FUNCTION)
                                        .function(FunctionCall.builder()
                                                .name(functionName)
                                                .arguments(functionArguments)
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.toolExecutionRequests())
                .containsExactly(ToolExecutionRequest.builder()
                        .name(functionName)
                        .arguments(functionArguments)
                        .build());
    }

    @Test
    void should_return_ai_message_with_toolExecutionRequests_and_text_when_tool_calls_and_content_are_both_present() {

        // given
        String functionName = "current_time";
        String functionArguments = "{}";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .content("Hello")
                                .toolCalls(ToolCall.builder()
                                        .type(FUNCTION)
                                        .function(FunctionCall.builder()
                                                .name(functionName)
                                                .arguments(functionArguments)
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.text()).isEqualTo("Hello");
        assertThat(aiMessage.toolExecutionRequests())
                .containsExactly(ToolExecutionRequest.builder()
                        .name(functionName)
                        .arguments(functionArguments)
                        .build());
    }

    @Test
    void should_map_tool_choice() {
        assertThat(toOpenAiToolChoice(ToolChoice.AUTO)).isEqualTo(ToolChoiceMode.AUTO);
        assertThat(toOpenAiToolChoice(ToolChoice.REQUIRED)).isEqualTo(ToolChoiceMode.REQUIRED);
        assertThat(toOpenAiToolChoice(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource
    void should_map_all_tool_choices(ToolChoice toolChoice) {
        assertThat(toOpenAiToolChoice(toolChoice)).isNotNull();
    }

    @Test
    void should_include_thinking_content_when_returnThinking_is_true() {
        // given
        String messageContent = "The answer is 1";
        String reasoningContent = "Let me think...";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .content(messageContent)
                                .reasoningContent(reasoningContent)
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response, true);

        // then
        assertThat(aiMessage.text()).isEqualTo(messageContent);
        assertThat(aiMessage.thinking()).isEqualTo(reasoningContent);
    }

    @Test
    void should_exclude_thinking_content_when_returnThinking_is_false() {
        // given
        String messageContent = "The answer is 1";
        String reasoningContent = "Let me think...";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .content(messageContent)
                                .reasoningContent(reasoningContent)
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response, false);

        // then
        assertThat(aiMessage.text()).isEqualTo(messageContent);
        assertThat(aiMessage.thinking()).isNull();
    }

    @Test
    void should_handle_tool_call_with_id() {
        // given
        String toolCallId = "id";
        String functionName = "check";
        String functionArguments = "{\"query\":\"OpenAI\"}";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder()
                                .toolCalls(singletonList(ToolCall.builder()
                                        .id(toolCallId)
                                        .type(FUNCTION)
                                        .function(FunctionCall.builder()
                                                .name(functionName)
                                                .arguments(functionArguments)
                                                .build())
                                        .build()))
                                .build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.toolExecutionRequests()).hasSize(1);
        ToolExecutionRequest request = aiMessage.toolExecutionRequests().get(0);
        assertThat(request.id()).isEqualTo(toolCallId);
        assertThat(request.name()).isEqualTo(functionName);
        assertThat(request.arguments()).isEqualTo(functionArguments);
    }

    @Test
    void should_return_null_text_when_content_is_null() {
        // given
        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(singletonList(ChatCompletionChoice.builder()
                        .message(AssistantMessage.builder().content(null).build())
                        .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.text()).isNull();
        assertThat(aiMessage.toolExecutionRequests()).isEmpty();
    }

    /**
     * Regression test for https://github.com/langchain4j/langchain4j/issues/4931
     * <p>
     * Some OpenAI-compatible providers return multiple choices:
     * - choices[0] contains assistant text
     * - choices[1] contains tool_calls
     * <p>
     * LangChain4j must NOT silently drop the tool_calls from later choices.
     */
    @Test
    void should_merge_tool_calls_from_later_choices_when_text_is_in_earlier_choice() {
        // given
        String textContent = "I will first inspect the target file.";
        String toolCallId = "call_123";
        String functionName = "readFile";
        String functionArguments = "{\"path\":\"src/main/java/example/Foo.java\"}";

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(Arrays.asList(
                        // choices[0]: text only
                        ChatCompletionChoice.builder()
                                .index(0)
                                .message(AssistantMessage.builder()
                                        .content(textContent)
                                        .build())
                                .build(),
                        // choices[1]: tool_calls only
                        ChatCompletionChoice.builder()
                                .index(1)
                                .message(AssistantMessage.builder()
                                        .content(null)
                                        .toolCalls(singletonList(ToolCall.builder()
                                                .id(toolCallId)
                                                .type(FUNCTION)
                                                .function(FunctionCall.builder()
                                                        .name(functionName)
                                                        .arguments(functionArguments)
                                                        .build())
                                                .build()))
                                        .build())
                                .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.text()).isEqualTo(textContent);
        assertThat(aiMessage.toolExecutionRequests()).hasSize(1);
        ToolExecutionRequest request = aiMessage.toolExecutionRequests().get(0);
        assertThat(request.id()).isEqualTo(toolCallId);
        assertThat(request.name()).isEqualTo(functionName);
        assertThat(request.arguments()).isEqualTo(functionArguments);
    }

    @Test
    void should_merge_tool_calls_from_multiple_choices() {
        // given
        String textContent = "Hello";

        ToolCall toolCall1 = ToolCall.builder()
                .id("call_1")
                .type(FUNCTION)
                .function(FunctionCall.builder()
                        .name("func_a")
                        .arguments("{\"a\":1}")
                        .build())
                .build();

        ToolCall toolCall2 = ToolCall.builder()
                .id("call_2")
                .type(FUNCTION)
                .function(FunctionCall.builder()
                        .name("func_b")
                        .arguments("{\"b\":2}")
                        .build())
                .build();

        ChatCompletionResponse response = ChatCompletionResponse.builder()
                .choices(Arrays.asList(
                        ChatCompletionChoice.builder()
                                .index(0)
                                .message(AssistantMessage.builder()
                                        .content(textContent)
                                        .toolCalls(singletonList(toolCall1))
                                        .build())
                                .build(),
                        ChatCompletionChoice.builder()
                                .index(1)
                                .message(AssistantMessage.builder()
                                        .toolCalls(singletonList(toolCall2))
                                        .build())
                                .build()))
                .build();

        // when
        AiMessage aiMessage = aiMessageFrom(response);

        // then
        assertThat(aiMessage.text()).isEqualTo(textContent);
        assertThat(aiMessage.toolExecutionRequests()).hasSize(2);
        assertThat(aiMessage.toolExecutionRequests().get(0).name()).isEqualTo("func_a");
        assertThat(aiMessage.toolExecutionRequests().get(1).name()).isEqualTo("func_b");
    }
}
