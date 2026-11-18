package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executor for Java code using Java Compiler API and separate process execution.
 *
 * Expected source format:
 * - A single public class with a main method
 * - Class name must match the filename (Main.java -> public class Main)
 * - Package declaration is optional but if present must match directory structure
 */
@Component
public class JavaExecutor implements CodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(JavaExecutor.class);

    private final ExecutionConfig config;

    // Pattern to extract class name from Java source
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
        "(?:public\\s+)?(?:class|interface|enum)\\s+(\\w+)\\s*(?:extends|implements|\\{)"
    );

    public JavaExecutor(ExecutionConfig config) {
        this.config = config;
    }

    @Override
    public String getLanguage() {
        return "java";
    }

    @Override
    public boolean supports(String language) {
        if (language == null) return false;
        String normalized = language.toLowerCase().trim();
        return "java".equals(normalized);
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        Instant startTime = Instant.now();
        String code = request.getCode();
        String language = "java";

        // Validate input
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.invalidRequest(language, "Code cannot be empty");
        }

        // Determine timeouts
        long compilationTimeoutMs = config.getJavaCompilationTimeoutMs();
        long executionTimeoutMs = getExecutionTimeout(request.getTimeoutMs());
        long maxOutputSize = getMaxOutputSize(request.getMaxOutputSize());

        // Create temporary directory for compilation
        Path tempDir = null;
        try {
            tempDir = createTempDirectory();
            log.debug("Created temp directory: {}", tempDir);

            // Extract class name from source code
            String className = extractClassName(code);
            if (className == null) {
                return ExecutionResult.failed(language, "Could not find public class in source code. Expected format: 'public class ClassName { public static void main(String[] args) { ... } }'",
                        Duration.between(startTime, Instant.now()).toMillis(), ExecutionStatus.COMPILATION_ERROR);
            }

            // Write source file
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.writeString(sourceFile, code, StandardCharsets.UTF_8);
            log.debug("Wrote source file: {}", sourceFile);

            // Compile the source code
            ExecutionResult compileResult = compileJava(sourceFile, tempDir, compilationTimeoutMs, maxOutputSize, startTime, language);
            if (!compileResult.isSuccess()) {
                return compileResult;
            }

            // Execute the compiled class
            ExecutionResult execResult = executeJava(className, tempDir, executionTimeoutMs, maxOutputSize, startTime, language);

            return execResult;

        } catch (IOException e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Java execution I/O error: {}", e.getMessage(), e);
            return ExecutionResult.failed(language, "I/O error: " + e.getMessage(), executionTime, ExecutionStatus.FAILED);

        } catch (Exception e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Java execution error: {}", e.getMessage(), e);
            return ExecutionResult.failed(language, "Execution error: " + e.getMessage(), executionTime, ExecutionStatus.FAILED);

        } finally {
            // Clean up temporary directory
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    private Path createTempDirectory() throws IOException {
        String prefix = "polyglotmesh-java-";
        if (config.getJavaTempDir() != null && !config.getJavaTempDir().isBlank()) {
            Path customTempDir = Paths.get(config.getJavaTempDir());
            if (Files.exists(customTempDir) && Files.isDirectory(customTempDir)) {
                return Files.createTempDirectory(customTempDir, prefix);
            }
        }
        return Files.createTempDirectory(prefix);
    }

    private String extractClassName(String code) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private ExecutionResult compileJava(Path sourceFile, Path tempDir, long timeoutMs, long maxOutputSize, Instant startTime, String language) {
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-d", tempDir.toString(),
                "-encoding", "UTF-8",
                sourceFile.toString()
        );
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Capture output with timeout
            String output = captureProcessOutput(process, timeoutMs, maxOutputSize);
            int exitCode = process.waitFor();

            long executionTime = Duration.between(startTime, Instant.now()).toMillis();

            if (exitCode != 0) {
                log.warn("Java compilation failed with exit code {}: {}", exitCode, output);
                return ExecutionResult.failed(language, "Compilation error:\n" + output, executionTime, ExecutionStatus.COMPILATION_ERROR);
            }

            log.debug("Java compilation succeeded in {} ms", executionTime);
            return ExecutionResult.success(language, "", executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            return ExecutionResult.failed(language, "Compilation timed out", executionTime, ExecutionStatus.TIMEOUT);

        } catch (IOException e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Java compilation I/O error: {}", e.getMessage(), e);
            return ExecutionResult.failed(language, "Compilation I/O error: " + e.getMessage(), executionTime, ExecutionStatus.FAILED);
        }
    }

    private ExecutionResult executeJava(String className, Path tempDir, long timeoutMs, long maxOutputSize, Instant startTime, String language) {
        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-cp", tempDir.toString(),
                className
        );
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Capture output with timeout
            String output = captureProcessOutput(process, timeoutMs, maxOutputSize);
            int exitCode = process.waitFor();

            long executionTime = Duration.between(startTime, Instant.now()).toMillis();

            if (exitCode != 0) {
                log.warn("Java execution failed with exit code {}: {}", exitCode, output);
                return ExecutionResult.failed(language, output, executionTime, ExecutionStatus.FAILED);
            }

            log.debug("Java execution succeeded in {} ms", executionTime);
            return ExecutionResult.success(language, output.trim(), executionTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            return ExecutionResult.timeout(language, executionTime);

        } catch (IOException e) {
            long executionTime = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Java execution I/O error: {}", e.getMessage(), e);
            return ExecutionResult.failed(language, "Execution I/O error: " + e.getMessage(), executionTime, ExecutionStatus.FAILED);
        }
    }

    private String captureProcessOutput(Process process, long timeoutMs, long maxOutputSize) throws IOException, InterruptedException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        InputStream inputStream = process.getInputStream();
        long startTime = System.currentTimeMillis();

        while (true) {
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                process.destroyForcibly();
                throw new InterruptedException("Process timed out after " + timeoutMs + " ms");
            }

            // Check if process has finished
            try {
                int exitCode = process.exitValue();
                // Process finished
                break;
            } catch (IllegalThreadStateException e) {
                // Process still running
            }

            // Read available output
            while (inputStream.available() > 0) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) break;
                outputStream.write(buffer, 0, bytesRead);

                // Check output size limit
                if (outputStream.size() > maxOutputSize) {
                    process.destroyForcibly();
                    String truncated = outputStream.toString(StandardCharsets.UTF_8);
                    return truncated + "\n... [output truncated, exceeded " + maxOutputSize + " bytes]";
                }
            }

            // Small sleep to avoid busy waiting
            Thread.sleep(10);
        }

        // Read any remaining output
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            if (outputStream.size() > maxOutputSize) {
                String truncated = outputStream.toString(StandardCharsets.UTF_8);
                return truncated + "\n... [output truncated, exceeded " + maxOutputSize + " bytes]";
            }
        }

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.debug("Failed to delete temp file {}: {}", path, e.getMessage());
                        }
                    });
            log.debug("Cleaned up temp directory: {}", tempDir);
        } catch (IOException e) {
            log.warn("Failed to clean up temp directory {}: {}", tempDir, e.getMessage());
        }
    }

    private long getExecutionTimeout(Long requestedTimeout) {
        if (requestedTimeout != null && requestedTimeout > 0 && requestedTimeout <= config.getMaxTimeoutMs()) {
            return requestedTimeout;
        }
        return config.getJavaExecutionTimeoutMs();
    }

    private long getMaxOutputSize(Long requestedMaxOutputSize) {
        if (requestedMaxOutputSize != null && requestedMaxOutputSize > 0 && requestedMaxOutputSize <= config.getMaxOutputSize()) {
            return requestedMaxOutputSize;
        }
        return config.getDefaultMaxOutputSize();
    }
}