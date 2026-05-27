package dev.langchain4j.model.openaiofficial;

import static org.assertj.core.api.Assertions.assertThat;

import com.openai.core.JsonValue;
import com.openai.models.responses.Tool;
import com.openai.models.responses.ToolSearchTool;
import com.openai.models.responses.WebSearchTool;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAiOfficialServerToolTest {

    @Test
    void should_create_tool_search_with_default_config() {
        Tool toolSearch = OpenAiOfficialServerTool.toolSearch();

        assertThat(toolSearch).isNotNull();
        assertThat(toolSearch.isSearch()).isTrue();
    }

    @Test
    void should_create_web_search_with_default_config() {
        Tool webSearch = OpenAiOfficialServerTool.webSearch();

        assertThat(webSearch).isNotNull();
        assertThat(webSearch.isWebSearch()).isTrue();
    }

    @Test
    void should_create_tool_search_with_builder() {
        Tool toolSearch = OpenAiOfficialServerTool.builder()
                .type("tool_search")
                .description("Search for relevant tools")
                .build();

        assertThat(toolSearch).isNotNull();
        assertThat(toolSearch.isSearch()).isTrue();

        ToolSearchTool searchTool = toolSearch.asSearch();
        assertThat(searchTool).isNotNull();
    }

    @Test
    void should_create_tool_search_with_client_execution_mode() {
        Tool toolSearch = OpenAiOfficialServerTool.builder()
                .type("tool_search")
                .execution("client")
                .parameters(JsonValue.from("""
                        {
                            "type": "object",
                            "properties": {
                                "goal": {"type": "string"}
                            },
                            "required": ["goal"]
                        }
                        """))
                .build();

        assertThat(toolSearch).isNotNull();
        assertThat(toolSearch.isSearch()).isTrue();
    }

    @Test
    void should_create_web_search_with_filters() {
        Tool webSearch = Tool.ofWebSearch(WebSearchTool.builder()
                .type(WebSearchTool.Type.of("web_search"))
                .filters(WebSearchTool.Filters.builder()
                        .allowedDomains(List.of("example.com"))
                        .build())
                .build());

        assertThat(webSearch).isNotNull();
        assertThat(webSearch.isWebSearch()).isTrue();
    }
}
