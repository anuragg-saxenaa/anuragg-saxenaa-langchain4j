package dev.langchain4j.mcp.client;

/**
 * Represents an update to an agent's goal during processing.
 *
 * <p>AgentGoalUpdate is used in the McpClientListener callback
 * {@link McpClientListener#onAgentGoalUpdated(AgentGoalUpdate)} to notify
 * listeners when an agent's goal changes during execution.</p>
 *
 * @param goalId unique identifier for the goal
 * @param oldValue the previous value of the goal (may be null)
 * @param newValue the new value of the goal
 * @param reason description of why the goal was updated
 */
public record AgentGoalUpdate(String goalId, String oldValue, String newValue, String reason) {}
