package dev.langchain4j.code.graalvm;

import dev.langchain4j.code.CodeExecutionEngine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraalVmJavaScriptExecutionEngineTest {

    CodeExecutionEngine engine = new GraalVmJavaScriptExecutionEngine();

    @Test
    void should_execute_code() {

        String code = """
                function fibonacci(n) {
                    if (n <= 1) return n;
                    return fibonacci(n - 1) + fibonacci(n - 2);
                }
                                
                fibonacci(10)
                """;

        String result = engine.execute(code);

        assertThat(result).isEqualTo("55");
    }

    @Test
    void should_include_captured_stdout_in_result() {

        String code = """
                console.log("hello");
                42
                """;

        String result = engine.execute(code);

        assertThat(result).contains("hello");
        assertThat(result).contains("42");
    }

    @Test
    void should_include_captured_stderr_in_result() {

        String code = """
                console.error("error message");
                100
                """;

        String result = engine.execute(code);

        assertThat(result).contains("error message");
        assertThat(result).contains("100");
    }

    @Test
    void should_return_only_result_when_no_output() {

        String code = "7 * 6";

        String result = engine.execute(code);


        assertThat(result).isEqualTo("42");
    }
}