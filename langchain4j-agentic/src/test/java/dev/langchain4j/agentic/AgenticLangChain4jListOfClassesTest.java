package dev.langchain4j.agentic;

import dev.langchain4j.agentic.annotation.Agent;
import dev.langchain4j.agentic.annotation.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

/**
 * Regression test for langchain4j/langchain4j#4887.
 * <p>
 * AgenticLangChain4j.agent(...) throws IllegalArgumentException:
 * "No agent method found in class: java.util.ImmutableCollections$ListN"
 * when passed a List of agent classes instead of individual classes.
 * <p>
 * Root cause: subAgents(Collection) called agentsToExecutors(collection)
 * which iterates collection elements as Object, calls agentToExecutor(Object)
 * which treats the List itself as an agent instance rather than iterating it.
 * <p>
 * Fix: subAgents(Collection) now checks if each element is a Class<?> and
 * handles it via agentToExecutor(Class<?>) instead of agentToExecutor(Object).
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_AGENTIC_IT", matches = "true")
class AgenticLangChain4jListOfClassesTest {

    @Test
    void shouldAcceptListOfAgentClasses() {
        ChatModel mockChatModel = mock(ChatModel.class);

        MyAgent myAgent = MyAgent.class;

        assertThatNoException().isThrownBy(() ->
                AiServices.builder(Supervisor.class)
                        .chatModel(mockChatModel)
                        .subAgents(List.of(myAgent))  // List<Class<?>, not varargs
                        .build()
        );
    }

    @Test
    void shouldAcceptMixedListOfClassAndInstance() {
        ChatModel mockChatModel = mock(ChatModel.class);

        assertThatNoException().isThrownBy(() ->
                AiServices.builder(Supervisor.class)
                        .chatModel(mockChatModel)
                        .subAgents(List.of(AnotherAgent.class, new DirectAgent(mockChatModel)))
                        .build()
        );
    }

    @SystemMessage("You are a supervisor.")
    interface Supervisor {
        @Agent(subAgents = {})
        String supervise(String task);
    }

    @SystemMessage("You are my agent.")
    interface MyAgent {
        @Agent
        String act(String instruction);
    }

    @SystemMessage("You are another agent.")
    interface AnotherAgent {
        @Agent
        String act(String instruction);
    }

    /** Non-declarative agent (direct builder pattern) */
    static class DirectAgent {
        private final ChatModel chatModel;
        DirectAgent(ChatModel chatModel) { this.chatModel = chatModel; }
        String doWork() { return "done"; }
    }
}