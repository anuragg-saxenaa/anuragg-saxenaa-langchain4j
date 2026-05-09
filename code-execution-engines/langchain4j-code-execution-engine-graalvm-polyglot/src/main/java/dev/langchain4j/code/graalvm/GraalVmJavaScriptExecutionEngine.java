package dev.langchain4j.code.graalvm;

import dev.langchain4j.code.CodeExecutionEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.SandboxPolicy;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static org.graalvm.polyglot.HostAccess.UNTRUSTED;
import static org.graalvm.polyglot.SandboxPolicy.CONSTRAINED;

/**
 * {@link CodeExecutionEngine} that uses GraalVM Polyglot/Truffle to execute provided JavaScript code.
 * Attention! It might be dangerous to execute the code, see {@link SandboxPolicy#CONSTRAINED}
 * and {@link HostAccess#UNTRUSTED} for more details.
 */
public class GraalVmJavaScriptExecutionEngine implements CodeExecutionEngine {

    /**
     * Pattern to match lines that are internal Truffle/GraalVM engine logs,
     * as opposed to actual output from the executed code.
     */
    private static final Pattern TRUFFLE_WARNING_LINE = Pattern.compile(
        "^\\s*(\\[To redirect|Truffle|\\[engine\\]|Use one of the following|For more information|WARNING:|--).*",
        Pattern.MULTILINE
    );

    @Override
    public String execute(String code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder("js")
            .sandbox(CONSTRAINED)
            .allowHostAccess(UNTRUSTED)
            .option("engine.WarnInterpreterOnly", "false")
            .out(outputStream)
            .err(outputStream)
            .build()) {
            Object result = context.eval("js", code).as(Object.class);
            String capturedOutput = outputStream.toString();
            String filteredOutput = TRUFFLE_WARNING_LINE.matcher(capturedOutput).replaceAll("");
            String userOutput = filteredOutput.trim();
            if (!userOutput.isEmpty()) {
                return userOutput + System.lineSeparator() + result;
            }
            return String.valueOf(result);
        }
    }
}
