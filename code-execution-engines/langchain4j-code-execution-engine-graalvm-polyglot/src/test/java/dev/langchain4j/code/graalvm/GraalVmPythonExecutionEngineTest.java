package dev.langchain4j.code.graalvm;

import dev.langchain4j.code.CodeExecutionEngine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraalVmPythonExecutionEngineTest {

    CodeExecutionEngine engine = new GraalVmPythonExecutionEngine();

    @Test
    void should_execute_code() {

        String code = """
                def fibonacci(n):
                    if n <= 1:
                        return n
                    else:
                        return fibonacci(n-1) + fibonacci(n-2)
                                
                fibonacci(10)
                """;

        String result = engine.execute(code);

        assertThat(result).isEqualTo("55");
    }

    @Test
    void should_include_captured_stdout_in_result() {

        String code = """
                print("hello")
                42
                """;

        String result = engine.execute(code);

        assertThat(result).contains("hello");
        assertThat(result).contains("42");
    }

    @Test
    void should_include_captured_stderr_in_result() {

        String code = "import sys; sys.stderr.write(\"error message\" + chr(10)); 100";

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
