package dev.langchain4j.agentic.patterns.goap;

import dev.langchain4j.agentic.planner.AgentInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for langchain4j #4986:
 * GOAP throws NPE when custom AgentListeners inject arbitrary variables
 * into AgenticScope that aren't graph dependencies.
 * <p>
 * The issue: When AgentListeners inject variables like STATE_KEY_INVOCATION_ID
 * into AgenticScope, these aren't registered as graph nodes. The search method
 * was passing null nodes to DependencyGraphSearch, causing NPE at line 208:
 * "Cannot invoke getOutputNodes() because activatedNode is null"
 */
public class GoalOrientedSearchGraphTest {

    /**
     * Tests that non-graph scope variables (injected by AgentListeners)
     * are filtered out before passing to DependencyGraphSearch.
     * <p>
     * Previously, the code would throw:
     * - NullPointerException in findActivatableNodes() when iterating activatedNodes
     * - Because null nodes were passed as preconditions
     */
    @Test
    void shouldFilterNullNodes_fromPreconditions() {
        // Create empty graph (no agents registered)
        var graph = new GoalOrientedSearchGraph(List.of());

        // Passing unregistered scope variables should not cause NPE
        // They should be filtered out before calling DependencyGraphSearch
        List<AgentInstance> result = graph.search(
                List.of("INVOCATION_COUNTER", "INVOCATION_TIMESTAMP", "CUSTOM_METADATA", "LAST_INVOCATION"),
                "finalResult"
        );

        // Should return empty list, not throw NPE
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleEmptyPreconditions() {
        var graph = new GoalOrientedSearchGraph(List.of());

        // Empty precondition list should also be handled gracefully
        List<AgentInstance> result = graph.search(List.of(), "nonExistentGoal");

        assertThat(result).isEmpty();
    }
}