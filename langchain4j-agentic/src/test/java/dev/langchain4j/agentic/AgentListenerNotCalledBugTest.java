package dev.langchain4j.agentic;

import static dev.langchain4j.agentic.Models.baseModel;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.AfterAgentToolExecution;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentRequest;
import dev.langchain4j.agentic.observability.AgentResponse;
import dev.langchain4j.agentic.observability.BeforeAgentToolExecution;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * Regression test for bug #4989: beforeAgentInvocation and afterAgentInvocation callbacks not called
 * in AgentInvocationHandler when using AgenticServices.agentBuilder() directly (non-Planner mode).
 */
public class AgentListenerNotCalledBugTest {

    public interface SimpleAgent {
        @Agent(value = "Simple agent for bug reproduction", outputKey = "result")
        @UserMessage("You are a helper assistant. {{input}}")
        String execute(@V("input") String input);
    }

    /**
     * Test tool that converts text to uppercase.
     */
    public static class TestTool {
        @Tool("Convert text to uppercase")
        public String toUpperCase(String text) {
            return text != null ? text.toUpperCase() : "NULL";
        }
    }

    /**
     * Mock ChatModel that simulates an LLM returning tool calls.
     */
    static class MockToolCallingChatModel implements ChatModel {
        private final String toolNameToCall;
        private int callCount = 0;

        public MockToolCallingChatModel(String toolNameToCall) {
            this.toolNameToCall = toolNameToCall;
        }

        @Override
        public ChatResponse chat(ChatRequest request) {
            callCount++;
            if (callCount == 1) {
                // First call: return tool call request
                AiMessage aiMessage = AiMessage.builder()
                        .toolExecutionRequests(List.of(
                                ToolExecutionRequest.builder()
                                        .name(toolNameToCall)
                                        .arguments("{\"text\": \"hello\"}")
                                        .build()))
                        .build();
                return ChatResponse.builder()
                        .aiMessage(aiMessage)
                        .build();
            }
            // Second call: return final result
            AiMessage finalMessage = AiMessage.builder()
                    .text("Tool execution complete, result is: HELLO")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(finalMessage)
                    .build();
        }
    }

    /**
     * Custom AgentListener to verify all callbacks are called.
     */
    static class TestAgentListener implements AgentListener {
        private final AtomicBoolean beforeInvocationCalled = new AtomicBoolean(false);
        private final AtomicBoolean afterInvocationCalled = new AtomicBoolean(false);
        private final AtomicBoolean beforeToolExecutionCalled = new AtomicBoolean(false);
        private final AtomicBoolean afterToolExecutionCalled = new AtomicBoolean(false);

        @Override
        public void beforeAgentInvocation(AgentRequest agentRequest) {
            beforeInvocationCalled.set(true);
            System.out.println("[TestListener] beforeAgentInvocation called: agent=" + agentRequest.agentName());
        }

        @Override
        public void afterAgentInvocation(AgentResponse agentResponse) {
            afterInvocationCalled.set(true);
            System.out.println("[TestListener] afterAgentInvocation called: agent=" + agentResponse.agentName());
        }

        @Override
        public void beforeAgentToolExecution(BeforeAgentToolExecution beforeAgentToolExecution) {
            beforeToolExecutionCalled.set(true);
            System.out.println("[TestListener] beforeAgentToolExecution called: tool="
                    + beforeAgentToolExecution.toolExecution().request().name());
        }

        @Override
        public void afterAgentToolExecution(AfterAgentToolExecution afterAgentToolExecution) {
            afterToolExecutionCalled.set(true);
            System.out.println("[TestListener] afterAgentToolExecution called: tool="
                    + afterAgentToolExecution.toolExecution().request().name());
        }

        public AtomicBoolean getBeforeInvocationCalled() {
            return beforeInvocationCalled;
        }

        public AtomicBoolean getAfterInvocationCalled() {
            return afterInvocationCalled;
        }

        public AtomicBoolean getBeforeToolExecutionCalled() {
            return beforeToolExecutionCalled;
        }

        public AtomicBoolean getAfterToolExecutionCalled() {
            return afterToolExecutionCalled;
        }
    }

    @Test
    public void beforeAgentInvocationAndAfterAgentInvocationCallbacksShouldBeCalled() {
        // Given: create an Agent that will trigger tool calls
        MockToolCallingChatModel mockChatModel = new MockToolCallingChatModel("toUpperCase");
        TestAgentListener listener = new TestAgentListener();

        SimpleAgent agent = AgenticServices
                .agentBuilder(SimpleAgent.class)
                .chatModel(mockChatModel)
                .tools(new TestTool())
                .listener(listener)
                .build();

        // When: invoke the Agent method
        String result = agent.execute("hello");

        // Then: verify tool execution callbacks are called
        assertThat(listener.getBeforeToolExecutionCalled().get())
                .as("beforeAgentToolExecution should be called")
                .isTrue();
        assertThat(listener.getAfterToolExecutionCalled().get())
                .as("afterAgentToolExecution should be called")
                .isTrue();

        // And: verify agent invocation callbacks are also called (this was the bug)
        assertThat(listener.getBeforeInvocationCalled().get())
                .as("beforeAgentInvocation should be called (BUG FIXED)")
                .isTrue();
        assertThat(listener.getAfterInvocationCalled().get())
                .as("afterAgentInvocation should be called (BUG FIXED)")
                .isTrue();

        System.out.println("Result: " + result);
    }
}
