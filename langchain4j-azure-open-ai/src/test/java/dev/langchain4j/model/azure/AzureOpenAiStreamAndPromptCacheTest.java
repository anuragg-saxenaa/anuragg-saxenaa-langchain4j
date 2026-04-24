package dev.langchain4j.model.azure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for stream and promptCacheKey parameters on Azure models.
 *
 * These tests verify that the builder methods exist and accept valid parameters.
 * Integration tests with actual Azure credentials are in AzureOpenAiStreamAndPromptCacheIT.
 */
class AzureOpenAiStreamAndPromptCacheTest {

    @Test
    void sync_model_builder_should_have_stream_method() {
        // Verify the builder accepts stream parameter
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .stream(true);

        // The builder should accept this without throwing
        assertThat(builder).isNotNull();
    }

    @Test
    void sync_model_builder_should_have_promptCacheKey_method() {
        // Verify the builder accepts promptCacheKey parameter
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .promptCacheKey("test-cache-key");

        assertThat(builder).isNotNull();
    }

    @Test
    void streaming_model_builder_should_have_promptCacheKey_method() {
        // Verify the streaming model builder accepts promptCacheKey parameter
        AzureOpenAiStreamingChatModel.Builder builder = AzureOpenAiStreamingChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .promptCacheKey("streaming-cache-key");

        assertThat(builder).isNotNull();
    }

    @Test
    void sync_model_builder_should_accept_both_parameters() {
        // Verify both parameters can be used together
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .stream(true)
                .promptCacheKey("combined-cache-key");

        assertThat(builder).isNotNull();
    }

    @Test
    void sync_model_builder_should_accept_stream_false() {
        // Verify stream=false is accepted
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .stream(false);

        assertThat(builder).isNotNull();
    }

    @Test
    void sync_model_builder_should_accept_null_stream() {
        // Verify null stream is accepted (default behavior)
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .stream(null);

        assertThat(builder).isNotNull();
    }

    @Test
    void sync_model_builder_should_accept_null_promptCacheKey() {
        // Verify null promptCacheKey is accepted (default behavior)
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint("https://test.openai.azure.com/")
                .apiKey("test-key")
                .deploymentName("test-model")
                .promptCacheKey(null);

        assertThat(builder).isNotNull();
    }
}
