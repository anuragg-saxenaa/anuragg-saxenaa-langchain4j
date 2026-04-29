package dev.langchain4j.model.openaiofficial;

import com.openai.core.JsonValue;
import com.openai.models.responses.Tool;
import com.openai.models.responses.ToolSearchTool;
import com.openai.models.responses.WebSearchTool;
import dev.langchain4j.Experimental;

/**
 * Helper class for creating server tools compatible with OpenAI's Responses API tool search workflow.
 * <p>
 * Supports:
 * <ul>
 *     <li>{@code tool_search} - Enables hosted tool search for dynamic tool loading</li>
 *     <li>{@code web_search} - Enables web search as a server tool</li>
 * </ul>
 * <p>
 * Example usage for hosted tool search:
 * <pre>{@code
 * OpenAiOfficialServerTool toolSearch = OpenAiOfficialServerTool.toolSearch();
 *
 * StreamingChatModel model = OpenAiOfficialResponsesStreamingChatModel.builder()
 *     .apiKey(System.getenv("OPENAI_API_KEY"))
 *     .modelName("gpt-5.4")
 *     .serverTools(toolSearch)
 *     .build();
 * }</pre>
 *
 * @see <a href="https://developers.openai.com/api/docs/guides/tools-tool-search">OpenAI Tool Search Documentation</a>
 */
@Experimental
public class OpenAiOfficialServerTool {

    private OpenAiOfficialServerTool() {
        // Utility class
    }

    /**
     * Creates a tool_search server tool that enables hosted tool search.
     * <p>
     * When combined with deferred function tools, the model can dynamically
     * search for and load only the tools it needs at runtime.
     *
     * @return a Tool configured for hosted tool search
     */
    public static Tool toolSearch() {
        return Tool.ofSearch(ToolSearchTool.builder()
                .type(JsonValue.from("tool_search"))
                .build());
    }

    /**
     * Creates a tool_search server tool with custom configuration.
     *
     * @param builder builder for configuring the tool search tool
     * @return a Tool configured for tool search
     */
    public static Tool toolSearch(ToolSearchToolBuilder builder) {
        return builder.build();
    }

    /**
     * Creates a web_search server tool.
     *
     * @return a Tool configured for web search
     */
    public static Tool webSearch() {
        return Tool.ofWebSearch(WebSearchTool.builder()
                .type(WebSearchTool.Type.of("web_search"))
                .build());
    }

    /**
     * Creates a web_search server tool with custom filters.
     *
     * @param builder builder for configuring the web search tool
     * @return a Tool configured for web search with the given filters
     */
    public static Tool webSearch(WebSearchToolBuilder builder) {
        return builder.build();
    }

    /**
     * Returns a new builder for creating tool_search tools.
     *
     * @return a new ToolSearchToolBuilder
     */
    public static ToolSearchToolBuilder builder() {
        return new ToolSearchToolBuilder();
    }

    /**
     * Builder for creating tool_search server tools with custom configuration.
     */
    public static class ToolSearchToolBuilder {

        private final ToolSearchTool.Builder builder = ToolSearchTool.builder();

        /**
         * Sets the type of the tool.
         *
         * @param type the tool type (e.g., "tool_search")
         * @return this builder
         */
        public ToolSearchToolBuilder type(String type) {
            builder.type(JsonValue.from(type));
            return this;
        }

        /**
         * Sets the type using a JsonValue for flexible configuration.
         *
         * @param type the tool type as JsonValue
         * @return this builder
         */
        public ToolSearchToolBuilder type(JsonValue type) {
            builder.type(type);
            return this;
        }

        /**
         * Sets the description for the tool search tool.
         *
         * @param description the description
         * @return this builder
         */
        public ToolSearchToolBuilder description(String description) {
            builder.description(description);
            return this;
        }

        /**
         * Sets the execution mode for tool search.
         * Use "server" for hosted tool search (default), or "client" for client-executed tool search.
         *
         * @param execution the execution mode ("server" or "client")
         * @return this builder
         */
        public ToolSearchToolBuilder execution(String execution) {
            builder.execution(JsonValue.from(execution));
            return this;
        }

        /**
         * Sets the parameters schema for client-executed tool search.
         *
         * @param parameters the parameters schema as JsonValue
         * @return this builder
         */
        public ToolSearchToolBuilder parameters(JsonValue parameters) {
            builder.parameters(parameters);
            return this;
        }

        /**
         * Builds the tool_search Tool.
         *
         * @return the configured Tool
         */
        public Tool build() {
            return Tool.ofSearch(builder.build());
        }
    }

    /**
     * Builder for creating web_search server tools with custom configuration.
     */
    public static class WebSearchToolBuilder {

        private final WebSearchTool.Builder builder = WebSearchTool.builder();

        /**
         * Sets the type of the tool.
         *
         * @param type the tool type (e.g., "web_search")
         * @return this builder
         */
        public WebSearchToolBuilder type(String type) {
            builder.type(WebSearchTool.Type.of(type));
            return this;
        }

        /**
         * Sets the search context type.
         *
         * @param searchContextSize the search context size
         * @return this builder
         */
        public WebSearchToolBuilder searchContextSize(WebSearchTool.SearchContextSize searchContextSize) {
            builder.searchContextSize(searchContextSize);
            return this;
        }

        /**
         * Sets filters for web search.
         *
         * @param filters the web search filters
         * @return this builder
         */
        public WebSearchToolBuilder filters(WebSearchTool.Filters filters) {
            builder.filters(filters);
            return this;
        }

        /**
         * Builds the web_search Tool.
         *
         * @return the configured Tool
         */
        public Tool build() {
            return Tool.ofWebSearch(builder.build());
        }
    }
}
