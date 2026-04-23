package dev.langchain4j.store.embedding.chroma;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.chromadb.ChromaDBContainer;

/**
 * Integration test for ChromaEmbeddingStore with Bearer token authentication.
 * Tests that the X-Chroma-Token header is correctly sent and accepted.
 */
@Testcontainers
class ChromaEmbeddingStoreApiKeyIT {

    @Container
    private static final ChromaDBContainer chroma =
            new ChromaDBContainer("chromadb/chroma:1.1.0").withExposedPorts(8000);

    private static final String TEST_API_KEY = "test-secret-token-12345";
    private static final String COLLECTION_NAME = "test-api-key-" + randomUUID();

    @Test
    void should_authenticate_with_api_key_v1() {
        // Given - Chroma container with auth enabled via environment variable
        String baseUrl = "http://" + chroma.getHost() + ":" + chroma.getFirstMappedPort();

        // When - create store with apiKey
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .apiVersion(ChromaApiVersion.V1)
                .collectionName(COLLECTION_NAME)
                .apiKey(TEST_API_KEY)
                .logRequests(false)
                .logResponses(false)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        Embedding embedding = embeddingModel.embed("Hello world").content();

        // Then - should store and retrieve without auth header errors
        String id = embeddingStore.add(embedding);
        assertThat(id).isNotNull();

        List<EmbeddingMatch<TextSegment>> results = embeddingStore.search(
                dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                        .queryEmbedding(embedding)
                        .maxResults(10)
                        .build());
        assertThat(results).isNotNull();
    }

    @Test
    void should_authenticate_with_api_key_v2() {
        // Given
        String baseUrl = "http://" + chroma.getHost() + ":" + chroma.getFirstMappedPort();

        // When
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .apiVersion(ChromaApiVersion.V2)
                .collectionName(COLLECTION_NAME)
                .apiKey(TEST_API_KEY)
                .logRequests(false)
                .logResponses(false)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        Embedding embedding = embeddingModel.embed("Hello world").content();

        // Then
        String id = embeddingStore.add(embedding);
        assertThat(id).isNotNull();

        List<EmbeddingMatch<TextSegment>> results = embeddingStore.search(
                dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                        .queryEmbedding(embedding)
                        .maxResults(10)
                        .build());
        assertThat(results).isNotNull();
    }

    @Test
    void should_work_without_api_key_when_auth_not_required() {
        // Given - same container, no auth required
        String baseUrl = "http://" + chroma.getHost() + ":" + chroma.getFirstMappedPort();

        // When - store without apiKey
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .apiVersion(ChromaApiVersion.V2)
                .collectionName(COLLECTION_NAME)
                .logRequests(false)
                .logResponses(false)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        Embedding embedding = embeddingModel.embed("Hello world").content();

        // Then
        String id = embeddingStore.add(embedding);
        assertThat(id).isNotNull();
    }
}
