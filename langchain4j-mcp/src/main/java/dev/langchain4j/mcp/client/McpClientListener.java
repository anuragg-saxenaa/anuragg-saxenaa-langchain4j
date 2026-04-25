package dev.langchain4j.mcp.client;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutionResult;
import java.util.Map;

/**
 * Listener interface for monitoring MCP client operations.
 */
public interface McpClientListener {

    /**
     * Called after the MCP client has been opened and is ready to use.
     */
    default void open() {}

    /**
     * Called after a tool has been executed, regardless of success or failure.
     * This is invoked after either {@link #afterExecuteTool} or {@link #onExecuteToolError}.
     *
     * @param context the context of the tool call
     * @param executionRequest the original tool execution request
     * @param result the result of the tool execution (may be null if execution failed)
     */
    default void onToolCallExecuted(
            McpCallContext context, ToolExecutionRequest executionRequest, ToolExecutionResult result) {}

    /**
     * Called when an agent goal has been updated during processing.
     *
     * @param goalUpdate the goal update information
     */
    default void onAgentGoalUpdated(AgentGoalUpdate goalUpdate) {}

    /**
     * Called when an agent goal has been completed.
     *
     * @param goalCompletion the goal completion information
     */
    default void onAgentGoalCompleted(AgentGoalCompletion goalCompletion) {}

    /**
     * Called when the MCP client is being closed. Use this to clean up any resources.
     */
    default void close() {}

    /**
     * Called before executing a tool.
     */
    default void beforeExecuteTool(McpCallContext context) {}

    /**
     * Called after executing a tool if the execution was successful, or if it resulted in an application-level error
     * (but not a protocol-level or communication error).
     */
    default void afterExecuteTool(McpCallContext context, ToolExecutionResult result, Map<String, Object> rawResult) {}

    /**
     * Called when a tool execution fails due to a protocol-level or communication error.
     */
    default void onExecuteToolError(McpCallContext context, Throwable error) {}

    /**
     * Called before getting a resource.
     */
    default void beforeResourceGet(McpCallContext context) {}

    /**
     * Called after getting a resource.
     */
    default void afterResourceGet(
            McpCallContext context, McpReadResourceResult result, Map<String, Object> rawResult) {}

    /**
     * Called when getting a resource fails.
     */
    default void onResourceGetError(McpCallContext context, Throwable error) {}

    /**
     * Called before getting a prompt.
     */
    default void beforePromptGet(McpCallContext context) {}

    /**
     * Called after getting a prompt.
     */
    default void afterPromptGet(McpCallContext context, McpGetPromptResult result, Map<String, Object> rawResult) {}

    /**
     * Called when getting a prompt fails.
     */
    default void onPromptGetError(McpCallContext context, Throwable error) {}
}
