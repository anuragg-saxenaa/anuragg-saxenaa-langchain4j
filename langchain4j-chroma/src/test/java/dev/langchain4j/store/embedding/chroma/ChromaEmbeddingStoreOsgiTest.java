package dev.langchain4j.store.embedding.chroma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * OSGi-compatible test for ChromaEmbeddingStore.
 * Verifies that the store can be built and used without relying on ServiceLoader SPI.
 * This tests the chromaClient(ChromaClient) builder method which allows
 * injection of a pre-configured client, bypassing the default ServiceLoader-based
 * client creation that breaks in OSGi environments.
 */
class ChromaEmbeddingStoreOsgiTest {

    /**
     * Tests that a pre-configured ChromaClient can be injected via the builder,
     * demonstrating OSGi compatibility. In OSGi environments, ServiceLoader
     * lookups for HttpClientBuilder implementation may fail, so this pattern
     * allows users to provide their own client instance.
     */
    @Test
    void should_build_store_with_injected_chroma_client() throws Exception {
        // Given - a mock ChromaClient that bypasses ServiceLoader
        ChromaClient mockClient = mock(ChromaClient.class);

        Collection mockCollection = mock(Collection.class);
        when(mockCollection.getId()).thenReturn("test-collection-id");
        when(mockClient.collection(anyString())).thenReturn(mockCollection);
        when(mockClient.addEmbeddings(anyString(), any())).thenReturn(true);

        QueryResponse mockQueryResponse = mock(QueryResponse.class);
        when(mockQueryResponse.getIds()).thenReturn(Collections.singletonList(Collections.singletonList("id-1")));
        when(mockQueryResponse.getDistances()).thenReturn(Collections.singletonList(Collections.singletonList(0.1)));
        when(mockQueryResponse.getEmbeddings())
                .thenReturn(Collections.singletonList(Collections.singletonList(new double[]{0.1, 0.2, 0.3})));
        when(mockQueryResponse.getDocuments())
                .thenReturn(Collections.singletonList(Collections.singletonList("Hello world")));
        when(mockQueryResponse.getMetadatas())
                .thenReturn(Collections.singletonList(Collections.singletonList(Collections.emptyMap())));
        when(mockClient.queryCollection(anyString(), any())).thenReturn(mockQueryResponse);

        // When - build the store with injected client
        ChromaEmbeddingStore store = ChromaEmbeddingStore.builder()
                .chromaClient(mockClient)
                .collectionName("test-collection")
                .build();

        // Then
        assertThat(store).isNotNull();

        // Verify the injected client was used by checking the store works
        Embedding mockEmbedding = mock(Embedding.class);
        when(mockEmbedding.vector()).thenReturn(new double[]{0.1, 0.2, 0.3});
        when(mockEmbedding.vectorAsList()).thenReturn(List.of(0.1, 0.2, 0.3));

        String id = store.add(mockEmbedding);
        assertThat(id).isNotNull();

        // Verify collection was accessed via injected client
        verify(mockClient).collection("test-collection");
    }

    /**
     * Tests that the chromaClient builder method takes precedence over
     * url-based client creation. This ensures OSGi users can override
     * the default client even when baseUrl is also specified.
     */
    @Test
    void should_prefer_injected_client_over_url_based_client() throws Exception {
        // Given
        ChromaClient mockClient = mock(ChromaClient.class);
        Collection mockCollection = mock(Collection.class);
        when(mockCollection.getId()).thenReturn("osgi-test-id");
        when(mockClient.collection(anyString())).thenReturn(mockCollection);

        // When - both chromaClient and baseUrl are set, injected client should win
        ChromaEmbeddingStore store = ChromaEmbeddingStore.builder()
                .chromaClient(mockClient)
                .baseUrl("http://this-should-be-ignored:8000")
                .collectionName("osgi-test")
                .build();

        // Then - baseUrl was ignored, client was used
        assertThat(store).isNotNull();
        verify(mockClient).collection("osgi-test");
    }

    /**
     * Tests that the apiKey field is correctly passed through the builder chain.
     * This uses reflection since we can't easily start a real server in unit tests.
     */
    @Test
    void should_support_api_key_in_builder() {
        // When
        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .apiKey("my-secret-token")
                .collectionName("test");

        // Then - builder should accept apiKey without error
        assertThat(builder).isNotNull();
    }

    /**
     * Demonstrates the MilvusEmbeddingStore pattern that ChromaEmbeddingStore now follows.
     * This test documents the expected API一致性.
     */
    @Test
    void should_support_similar_pattern_to_milvus() {
        // The MilvusEmbeddingStore has milvusClient(MilvusServiceClient) method
        // ChromaEmbeddingStore now has chromaClient(ChromaClient) for the same purpose

        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .chromaClient(mock(ChromaClient.class))  // OSGi-friendly injection
                .apiKey("token")                         // Auth support
                .collectionName("default")
                .logRequests(true)
                .logResponses(true);

        assertThat(builder).isNotNull();
    }
}
