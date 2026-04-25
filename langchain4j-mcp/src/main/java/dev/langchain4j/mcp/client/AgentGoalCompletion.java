package dev.langchain4j.mcp.client;

/**
 * Represents the completion of an agent's goal.
 *
 * <p>AgentGoalCompletion is used in the McpClientListener callback
 * {@link McpClientListener#onAgentGoalCompleted(AgentGoalCompletion)} to notify
 * listeners when an agent's goal has been completed.</p>
 *
 * @param goalId unique identifier for the goal
 * @param finalValue the final value of the goal
 * @param success whether the goal was completed successfully
 * @param completionReason description of how/why the goal completed
 */
public record AgentGoalCompletion(String goalId, String finalValue, boolean success, String completionReason) {}
