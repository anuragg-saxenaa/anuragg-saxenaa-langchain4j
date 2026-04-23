package dev.langchain4j.model.azure;

import static dev.langchain4j.model.azure.AzureModelBuilders.getAzureOpenaiEndpoint;
import static dev.langchain4j.model.azure.AzureModelBuilders.getAzureOpenaiKey;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration tests for Azure OpenAI streaming and prompt cache parameters (issue #5005).
 * 
 * These tests verify that the stream and promptCacheKey parameters are properly
 * exposed in the SDK builder and can be used when making requests to Azure OpenAI.
 * 
 * Note: The stream parameter uses an internal SDK workaround (ChatCompletionsOptionsAccessHelper)
 * since setStream() was made private in the Azure SDK to reduce SSE log pollution.
 * 
 * Note: promptCacheKey relies on Azure OpenAI service-side support and requires
 * deployments with prompt caching enabled.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
class AzureOpenAiStreamAndPromptCacheIT {

    @Test
    void should_support_stream_parameter_on_sync_model() {
        // given - build model with stream=true
        // Note: stream parameter on sync model is for API flexibility;
        // actual streaming requires using AzureOpenAiStreamingChatModel
        ChatModel modelWithStream = AzureOpenAiChatModel.builder()
                .endpoint(getAzureOpenaiEndpoint())
                .apiKey(getAzureOpenaiKey())
                .deploymentName("gpt-4o-mini")
                .stream(true)
                .maxCompletionTokens(50)
                .logRequestsAndResponses(true)
                .build();

        // when - use chat() which returns String
        String response = modelWithStream.chat("What is 1+1?");

        // then
        assertThat(response).isNotBlank();
    }

    @Test
    void should_support_stream_false_on_sync_model() {
        // given - build model with stream=false (explicit non-streaming)
        ChatModel modelNoStream = AzureOpenAiChatModel.builder()
                .endpoint(getAzureOpenaiEndpoint())
                .apiKey(getAzureOpenaiKey())
                .deploymentName("gpt-4o-mini")
                .stream(false)
                .maxCompletionTokens(50)
                .logRequestsAndResponses(true)
                .build();

        // when
        String response = modelNoStream.chat("What is 2+2?");

        // then
        assertThat(response).isNotBlank();
    }

    @Test
    void should_support_promptCacheKey_parameter() {
        // given - build model with promptCacheKey
        // Note: prompt caching must be enabled on the Azure deployment
        ChatModel model = AzureOpenAiChatModel.builder()
                .endpoint(getAzureOpenaiEndpoint())
                .apiKey(getAzureOpenaiKey())
                .deploymentName("gpt-4o-mini")
                .promptCacheKey("default-prompt-cache-key")
                .maxCompletionTokens(50)
                .logRequestsAndResponses(true)
                .build();

        // when
        String response = model.chat("What is the capital of France?");

        // then
        assertThat(response).isNotBlank();
    }

    @Test
    void should_support_streaming_model_with_promptCacheKey() {
        // given
        // Note: AzureOpenAiStreamingChatModel always uses streaming internally
        // promptCacheKey can be used to leverage prompt caching with streaming
        AzureOpenAiStreamingChatModel streamingModel = AzureOpenAiStreamingChatModel.builder()
                .endpoint(getAzureOpenaiEndpoint())
                .apiKey(getAzureOpenaiKey())
                .deploymentName("gpt-4o-mini")
                .promptCacheKey("streaming-prompt-cache-key")
                .maxCompletionTokens(50)
                .logRequestsAndResponses(true)
                .build();

        // Verify the model is built correctly
        assertThat(streamingModel).isNotNull();
        assertThat(streamingModel.defaultRequestParameters()).isNotNull();
    }

    @Test
    void should_support_both_stream_and_promptCacheKey_together() {
        // given - test that both parameters can be set together on sync model
        ChatModel model = AzureOpenAiChatModel.builder()
                .endpoint(getAzureOpenaiEndpoint())
                .apiKey(getAzureOpenaiKey())
                .deploymentName("gpt-4o-mini")
                .stream(true)
                .promptCacheKey("combined-cache-key")
                .maxCompletionTokens(50)
                .logRequestsAndResponses(true)
                .build();

        // when
        String response = model.chat("Hello world");

        // then
        assertThat(response).isNotBlank();
    }
}