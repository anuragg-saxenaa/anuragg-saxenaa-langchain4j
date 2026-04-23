package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.invocation.InvocationContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A configurable pipeline for transforming {@link UserMessage}s during AI Service invocation.
 * <p>
 * The pipeline consists of 5 stages, executed in order:
 * <ol>
 *   <li><b>Template resolution</b> - Creates initial message from {@code @UserMessage} template + {@code @V} variables
 *       (always executes, not customizable)</li>
 *   <li><b>RAG augmentation</b> - Appends retrieved content via {@code RetrievalAugmentor}</li>
 *   <li><b>Content injection</b> - Adds {@code Image}, {@code Audio}, {@code Pdf}, etc. from method args annotated with {@code @UserMessage}</li>
 *   <li><b>Input guardrails</b> - Validates/rewrites the message via configured {@code InputGuardrail}s</li>
 *   <li><b>Output format instructions</b> - Appends format instructions (e.g. "respond in JSON...")</li>
 * </ol>
 * <p>
 * Custom steps can be registered via {@link Builder} to intercept stages 2, 3, and 4.
 * Each custom step receives the current {@link UserMessage} and returns a (potentially modified) one.
 * <p>
 * Default behavior: all stages execute as no-op passthrough (identity function), meaning
 * the message passes through unchanged unless custom steps or built-in processors modify it.
 */
@Internal
public class UserMessageTransformationPipeline {

    /**
     * A step in the content injection stage (stage 3).
     * Receives the current {@link UserMessage} and method metadata, returns a (potentially modified) one.
     */
    @FunctionalInterface
    public interface ContentInjectionStep
            extends BiFunction<UserMessage, InvocationContext, UserMessage> {}

    /**
     * A step in the input guardrail stage (stage 4).
     * Unlike {@link dev.langchain4j.guardrail.InputGuardrail} which may block/abort, this step
     * always rewrites the message (returning a new or modified {@link UserMessage}).
     */
    @FunctionalInterface
    public interface InputGuardrailRewriter
            extends BiFunction<UserMessage, InvocationContext, UserMessage> {}

    private final List<ContentInjectionStep> contentInjectionSteps;
    private final List<InputGuardrailRewriter> inputGuardrailRewriters;

    private UserMessageTransformationPipeline(
            List<ContentInjectionStep> contentInjectionSteps,
            List<InputGuardrailRewriter> inputGuardrailRewriters) {
        this.contentInjectionSteps = contentInjectionSteps != null ? Collections.unmodifiableList(new ArrayList<>(contentInjectionSteps)) : Collections.emptyList();
        this.inputGuardrailRewriters = inputGuardrailRewriters != null ? Collections.unmodifiableList(new ArrayList<>(inputGuardrailRewriters)) : Collections.emptyList();
    }

    /**
     * Returns all registered content injection steps.
     */
    public List<ContentInjectionStep> contentInjectionSteps() {
        return contentInjectionSteps;
    }

    /**
     * Returns all registered input guardrail rewriters.
     */
    public List<InputGuardrailRewriter> inputGuardrailRewriters() {
        return inputGuardrailRewriters;
    }

    /**
     * Applies all content injection steps to the given user message.
     * Steps are applied in registration order.
     *
     * @param userMessage the current user message
     * @param invocationContext the invocation context
     * @return the transformed user message
     */
    public UserMessage applyContentInjectionSteps(UserMessage userMessage, InvocationContext invocationContext) {
        UserMessage result = userMessage;
        for (ContentInjectionStep step : contentInjectionSteps) {
            result = step.apply(result, invocationContext);
        }
        return result;
    }

    /**
     * Applies all input guardrail rewriters to the given user message.
     * Rewriters are applied in registration order.
     *
     * @param userMessage the current user message
     * @param invocationContext the invocation context
     * @return the rewritten user message
     */
    public UserMessage applyInputGuardrailRewriters(UserMessage userMessage, InvocationContext invocationContext) {
        UserMessage result = userMessage;
        for (InputGuardrailRewriter rewriter : inputGuardrailRewriters) {
            result = rewriter.apply(result, invocationContext);
        }
        return result;
    }

    /**
     * Creates a new pipeline that is a copy of this one with the given content injection step added.
     *
     * @param step the step to add
     * @return a new pipeline with the step added
     */
    public UserMessageTransformationPipeline withContentInjectionStep(ContentInjectionStep step) {
        List<ContentInjectionStep> newSteps = new ArrayList<>(contentInjectionSteps);
        newSteps.add(step);
        return new UserMessageTransformationPipeline(newSteps, inputGuardrailRewriters);
    }

    /**
     * Creates a new pipeline that is a copy of this one with the given input guardrail rewriter added.
     *
     * @param rewriter the rewriter to add
     * @return a new pipeline with the rewriter added
     */
    public UserMessageTransformationPipeline withInputGuardrailRewriter(InputGuardrailRewriter rewriter) {
        List<InputGuardrailRewriter> newRewriters = new ArrayList<>(inputGuardrailRewriters);
        newRewriters.add(rewriter);
        return new UserMessageTransformationPipeline(contentInjectionSteps, newRewriters);
    }

    /**
     * Returns an empty pipeline (all stages are no-op passthrough).
     */
    public static UserMessageTransformationPipeline empty() {
        return new UserMessageTransformationPipeline(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Creates a new builder for {@link UserMessageTransformationPipeline}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<ContentInjectionStep> contentInjectionSteps = new ArrayList<>();
        private List<InputGuardrailRewriter> inputGuardrailRewriters = new ArrayList<>();

        /**
         * Registers a custom content injection step (stage 3).
         * Steps are applied in registration order.
         *
         * @param step the step to register
         * @return this builder
         */
        public Builder registerContentInjectionStep(ContentInjectionStep step) {
            if (step != null) {
                contentInjectionSteps.add(step);
            }
            return this;
        }

        /**
         * Registers a custom input guardrail rewriter (stage 4).
         * Rewriters are applied in registration order.
         *
         * @param rewriter the rewriter to register
         * @return this builder
         */
        public Builder registerInputGuardrailRewriter(InputGuardrailRewriter rewriter) {
            if (rewriter != null) {
                inputGuardrailRewriters.add(rewriter);
            }
            return this;
        }

        /**
         * Builds the {@link UserMessageTransformationPipeline}.
         *
         * @return a new pipeline
         */
        public UserMessageTransformationPipeline build() {
            return new UserMessageTransformationPipeline(contentInjectionSteps, inputGuardrailRewriters);
        }
    }
}
