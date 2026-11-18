package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Executor for Python code using GraalPy.
 */
@Component
public class PythonExecutor implements CodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(PythonExecutor.class);

    private final ExecutionConfig config;

    public PythonExecutor(ExecutionConfig config) {
        this.config = config;
    }

    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public boolean supports(String language) {
        if (language == null) return false;
        String normalized = language.toLowerCase().trim();
        return "python".equals(normalized) || "py".equals(normalized);
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        Instant startTime = Instant.now();
        String code = request.getCode();
        String language = "python";

        // Validate input
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.invalidRequest(language, "Code cannot be empty");
        }

        // Determine timeout
        long timeoutMs = getTimeout(request.getTimeoutMs());
        long maxOutputSize = getMaxOutputSize(request.getMaxOutputSize());

        // Create output capture streams
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream outputPrintStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
        PrintStream errorPrintStream = new PrintStream(errorStream, true, StandardCharsets.UTF_8);

        Context context = null;
        try {
            // Build GraalVM context with security restrictions
            Context.Builder contextBuilder = Context.newBuilder("python")
                    .out(outputPrintStream)
                    .err(errorPrintStream);

            if (config.isEnableSecurityRestrictions()) {
                contextBuilder.allowHostAccess(config.isAllowHostAccess() ? org.graalvm.polyglot.HostAccess.ALL : org.graalvm.polyglot.HostAccess.NONE)
                        .allowHostClassLookup(className -> config.isAllowHostClassLookup())
                        .allowNativeAccess(config.isAllowNativeAccess())
                        .allowIO(config.isAllowIO())
                        .allowExperimentalOptions(config.isAllowExperimentalOptions());
            }

            context = contextBuilder.build();

            // Execute with timeout
            ExecutionResult result = executeWithTimeout(context, code, timeoutMs, maxOutputSize, outputStream, errorStream, startTime);

            log.debug("Python execution completed in {} ms with status: {}",
                    result.getExecutionTime(), result.getStatus());

            return result;

        } catch (PolyglotException e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.warn("Python execution failed: {}", e.getMessage());
            return handlePolyglotException(e, language, executionTime);

        } catch (Exception e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Python execution error: {}", e.getMessage(), e);
            return ExecutionResult.failed(language, "Execution error: " + e.getMessage(), executionTime, ExecutionStatus.FAILED);

        } finally {
            // Close context and streams
            closeResources(context, outputPrintStream, errorPrintStream);
        }
    }

    private ExecutionResult executeWithTimeout(Context context, String code, long timeoutMs,
                                               long maxOutputSize, ByteArrayOutputStream outputStream,
                                               ByteArrayOutputStream errorStream, Instant startTime) {

        // Use a separate thread for execution with timeout
        ExecutionTask task = new ExecutionTask(context, code);
        Thread executionThread = new Thread(task);
        executionThread.start();

        try {
            executionThread.join(timeoutMs);

            if (executionThread.isAlive()) {
                // Timeout - interrupt and close context
                executionThread.interrupt();
                if (context != null) {
                    try {
                        context.close(true); // Force close
                    } catch (Exception e) {
                        log.warn("Error closing context after timeout: {}", e.getMessage());
                    }
                }
                long executionTime = Duration.between(startTime, Instant.now()).toMillis();
                return ExecutionResult.timeout("python", executionTime);
            }

            // Check for execution result
            if (task.exception != null) {
                if (task.exception instanceof RuntimeException) {
                    throw (RuntimeException) task.exception;
                }
                throw new RuntimeException(task.exception);
            }

            long executionTime = Duration.between(startTime, Instant.now()).toMillis();

            // Capture output with size limit
            String output = captureOutput(outputStream, maxOutputSize);
            String error = captureOutput(errorStream, maxOutputSize);

            return ExecutionResult.success("python", output, executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            return ExecutionResult.timeout("python", executionTime);
        }
    }

    private ExecutionResult handlePolyglotException(PolyglotException e, String language, long executionTime) {
        String errorMessage = e.getMessage();

        // Check if it's a syntax error
        if (e.isSyntaxError()) {
            return ExecutionResult.failed(language, "Syntax error: " + errorMessage, executionTime, ExecutionStatus.FAILED);
        }

        // Check for specific Python exceptions
        if (e.isGuestException()) {
            // Get the exception type from the guest language
            Value exception = e.getGuestObject();
            if (exception != null && exception.hasMember("__class__")) {
                String exceptionType = exception.getMember("__class__").getMember("__name__").asString();
                if ("ZeroDivisionError".equals(exceptionType)) {
                    return ExecutionResult.failed(language, "ZeroDivisionError: " + errorMessage, executionTime, ExecutionStatus.FAILED);
                }
                return ExecutionResult.failed(language, exceptionType + ": " + errorMessage, executionTime, ExecutionStatus.FAILED);
            }
        }

        return ExecutionResult.failed(language, errorMessage, executionTime, ExecutionStatus.FAILED);
    }

    private String captureOutput(ByteArrayOutputStream stream, long maxSize) {
        byte[] bytes = stream.toByteArray();
        if (bytes.length > maxSize) {
            String truncated = new String(bytes, 0, (int) maxSize, StandardCharsets.UTF_8);
            return truncated + "\n... [output truncated, exceeded " + maxSize + " bytes]";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private long getTimeout(Long requestedTimeout) {
        if (requestedTimeout != null && requestedTimeout > 0 && requestedTimeout <= config.getMaxTimeoutMs()) {
            return requestedTimeout;
        }
        return config.getDefaultTimeoutMs();
    }

    private long getMaxOutputSize(Long requestedMaxOutputSize) {
        if (requestedMaxOutputSize != null && requestedMaxOutputSize > 0 && requestedMaxOutputSize <= config.getMaxOutputSize()) {
            return requestedMaxOutputSize;
        }
        return config.getDefaultMaxOutputSize();
    }

    private void closeResources(Context context, PrintStream outputStream, PrintStream errorStream) {
        try {
            if (outputStream != null) outputStream.close();
        } catch (Exception e) {
            log.debug("Error closing output stream: {}", e.getMessage());
        }
        try {
            if (errorStream != null) errorStream.close();
        } catch (Exception e) {
            log.debug("Error closing error stream: {}", e.getMessage());
        }
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            log.debug("Error closing context: {}", e.getMessage());
        }
    }

    /**
     * Task to execute Python code in a separate thread.
     */
    private static class ExecutionTask implements Runnable {
        private final Context context;
        private final String code;
        volatile Exception exception;

        ExecutionTask(Context context, String code) {
            this.context = context;
            this.code = code;
        }

        @Override
        public void run() {
            try {
                Source source = Source.newBuilder("python", code, "PythonCode").build();
                context.eval(source);
            } catch (Exception e) {
                this.exception = e;
            }
        }
    }
}