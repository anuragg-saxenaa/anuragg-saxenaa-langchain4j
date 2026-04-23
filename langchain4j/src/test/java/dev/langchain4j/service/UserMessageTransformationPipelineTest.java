package dev.langchain4j.service;

import static dev.langchain4j.service.AiServicesIT.verifyNoMoreInteractionsFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.model.chat.request.ChatRequest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMessageTransformationPipelineTest {

    @Spy
    ChatModel chatModel = ChatModelMock.thatAlwaysResponds("ok");

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractionsFor(chatModel);
    }

    interface SimpleChat {
        String chat(String message);
    }

    @Test
    void custom_content_injector_should_add_timestamp_to_user_messages() {
        // given
        Instant fixedTime = Instant.parse("2025-01-15T10:30:00Z");

        UserMessageTransformationPipeline.ContentInjectionStep timestampInjector =
                (userMessage, invocationContext) -> {
                    String timestamp = "<!-- timestamp: " + fixedTime + " -->";
                    return userMessage.toBuilder()
                            .contents(List.of(TextContent.from(
                                    timestamp + " " + userMessage.singleText())))
                            .build();
                };

        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                .registerContentInjectionStep(timestampInjector)
                .build();

        // when
        String response = chat.chat("What is the time?");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        assertThat(userMessage.singleText()).contains("<!-- timestamp: 2025-01-15T10:30:00Z -->");
        assertThat(response).isEqualTo("ok");
    }

    @Test
    void custom_guardrail_rewriter_should_prepend_system_instructions() {
        // given
        UserMessageTransformationPipeline.InputGuardrailRewriter systemPrepender =
                (userMessage, invocationContext) -> {
                    String prefixedText = "IMPORTANT: Always respond in short sentences. "
                            + userMessage.singleText();
                    return userMessage.toBuilder()
                            .contents(List.of(TextContent.from(prefixedText)))
                            .build();
                };

        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                .registerInputGuardrailRewriter(systemPrepender)
                .build();

        // when
        String response = chat.chat("Tell me a joke.");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        assertThat(userMessage.singleText()).startsWith("IMPORTANT: Always respond in short sentences.");
        assertThat(response).isEqualTo("ok");
    }

    @Test
    void multiple_content_injection_steps_should_be_applied_in_order() {
        // given
        UserMessageTransformationPipeline.ContentInjectionStep step1 =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[step1] " + userMessage.singleText())))
                        .build();

        UserMessageTransformationPipeline.ContentInjectionStep step2 =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[step2] " + userMessage.singleText())))
                        .build();

        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                .registerContentInjectionStep(step1)
                .registerContentInjectionStep(step2)
                .build();

        // when
        chat.chat("hello");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        // step1 runs first, then step2 runs on step1's result
        assertThat(userMessage.singleText()).contains("[step2]");
        assertThat(userMessage.singleText()).contains("[step1]");
    }

    @Test
    void multiple_input_guardrail_rewriters_should_be_applied_in_order() {
        // given
        UserMessageTransformationPipeline.InputGuardrailRewriter rewriter1 =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[rewrite1] " + userMessage.singleText())))
                        .build();

        UserMessageTransformationPipeline.InputGuardrailRewriter rewriter2 =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[rewrite2] " + userMessage.singleText())))
                        .build();

        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                .registerInputGuardrailRewriter(rewriter1)
                .registerInputGuardrailRewriter(rewriter2)
                .build();

        // when
        chat.chat("test");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        assertThat(userMessage.singleText()).contains("[rewrite2]");
        assertThat(userMessage.singleText()).contains("[rewrite1]");
    }

    @Test
    void pipeline_with_no_custom_steps_should_be_noop_passthrough() {
        // given
        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                // no custom steps registered
                .build();

        // when
        chat.chat("plain message");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        assertThat(userMessage.singleText()).isEqualTo("plain message");
    }

    @Test
    void both_content_injection_and_guardrail_rewriter_should_work_together() {
        // given
        UserMessageTransformationPipeline.ContentInjectionStep timestampInjector =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[injected] " + userMessage.singleText())))
                        .build();

        UserMessageTransformationPipeline.InputGuardrailRewriter policyRewriter =
                (userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[policy] " + userMessage.singleText())))
                        .build();

        SimpleChat chat = AiServices.builder(SimpleChat.class)
                .chatModel(chatModel)
                .registerContentInjectionStep(timestampInjector)
                .registerInputGuardrailRewriter(policyRewriter)
                .build();

        // when
        chat.chat("request");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        // content injection runs first (stage 3), then guardrail rewriter (stage 4)
        assertThat(userMessage.singleText()).contains("[policy]");
        assertThat(userMessage.singleText()).contains("[injected]");
    }

    @Test
    void pipeline_builder_should_support_fluent_registration() {
        // given - construct pipeline directly then pass via AiServices.builder(context)
        UserMessageTransformationPipeline pipeline = UserMessageTransformationPipeline.builder()
                .registerContentInjectionStep((userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[built] " + userMessage.singleText())))
                        .build())
                .registerInputGuardrailRewriter((userMessage, ctx) -> userMessage.toBuilder()
                        .contents(List.of(TextContent.from("[rewritten] " + userMessage.singleText())))
                        .build())
                .build();

        AiServiceContext ctx = AiServiceContext.create(SimpleChat.class);
        ctx.userMessageTransformationPipeline = pipeline;

        SimpleChat chat = AiServices.<SimpleChat>builder(ctx)
                .chatModel(chatModel)
                .build();

        // when
        chat.chat("test");

        // then
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chatModel).chat(captor.capture());

        UserMessage userMessage = (UserMessage) captor.getValue().messages().get(0);
        assertThat(userMessage.singleText()).contains("[rewritten]");
        assertThat(userMessage.singleText()).contains("[built]");
    }
}
