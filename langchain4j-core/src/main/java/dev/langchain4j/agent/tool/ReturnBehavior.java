package dev.langchain4j.agent.tool;

import dev.langchain4j.Experimental;

/**
 * Defines the behavior of a tool's return value when called by a language model.
 */
@Experimental
public enum ReturnBehavior {

    /**
     * The value returned by the tool is sent back to the LLM for further processing.
     * This is the default behavior.
     */
    TO_LLM,

    /**
     * Returns immediately to the caller the value returned by the tool without allowing the LLM
     * to further process it. Immediate return is only allowed on AI services returning {@code dev.langchain4j.service.Result},
     * while a {@code RuntimeException} will be thrown attempting to use a tool with immediate return with an
     * AI service having a different return type.
     */
    IMMEDIATE,

    /**
     * Returns immediately to the caller the value returned by the tool <b>only if it is the last tool
     * executed in the current batch</b>, without allowing the LLM to further process it.
     * This is an efficiency optimization: when the LLM calls multiple tools in a batch and this tool
     * is the last one to return, we skip the unnecessary re-send to the LLM.
     * <p>
     * If the tool is executed in parallel with other tools (multiple tools in the same batch),
     * processing continues as normal unless this tool is the final one to return.
     * <p>
     * Immediate return is only allowed on AI services returning {@code dev.langchain4j.service.Result},
     * while a {@code RuntimeException} will be thrown attempting to use a tool with immediate return with an
     * AI service having a different return type.
     *
     * @since 1.14.0
     */
    IMMEDIATE_IF_LAST;
}
