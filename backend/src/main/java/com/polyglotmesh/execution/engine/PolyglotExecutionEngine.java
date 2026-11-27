package com.polyglotmesh.execution.engine;

import com.polyglotmesh.execution.executor.CodeExecutor;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import com.polyglotmesh.execution.exception.UnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main execution engine that routes code execution requests to appropriate language executors.
 */
@Service
public class PolyglotExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(PolyglotExecutionEngine.class);

    private final Map<String, CodeExecutor> executors = new ConcurrentHashMap<>();

    public PolyglotExecutionEngine(List<CodeExecutor> executorList) {
        for (CodeExecutor executor : executorList) {
            executors.put(executor.getLanguage().toLowerCase(), executor);
            log.info("Registered executor for language: {}", executor.getLanguage());
        }
    }

    /**
     * Execute code in the specified language.
     *
     * @param request the execution request containing language, code, and optional configuration
     * @return the execution result
     */
    public ExecutionResult execute(ExecutionRequest request) {
        if (request == null) {
            log.warn("Received null execution request");
            return ExecutionResult.invalidRequest("unknown", "Request cannot be null");
        }

        String language = request.getLanguage();
        if (language == null || language.trim().isEmpty()) {
            log.warn("Received request with empty language");
            return ExecutionResult.invalidRequest("unknown", "Language is required");
        }

        String normalizedLanguage = language.toLowerCase().trim();
        log.debug("Executing code for language: {} (normalized: {})", language, normalizedLanguage);

        // Find matching executor
        CodeExecutor executor = findExecutor(normalizedLanguage);
        if (executor == null) {
            log.warn("Unsupported language requested: {}", language);
            return ExecutionResult.unsupportedLanguage(language);
        }

        try {
            ExecutionResult result = executor.execute(request);
            log.info("Execution completed for language: {} with status: {} in {} ms",
                    normalizedLanguage, result.getStatus(), result.getExecutionTime());
            return result;
        } catch (UnsupportedLanguageException e) {
            log.warn("Unsupported language: {}", language);
            return ExecutionResult.unsupportedLanguage(language);
        } catch (Exception e) {
            log.error("Unexpected error during execution for language {}: {}", normalizedLanguage, e.getMessage(), e);
            return ExecutionResult.failed(normalizedLanguage, "Internal execution error: " + e.getMessage(),
                    0, ExecutionStatus.FAILED);
        }
    }

    /**
     * Find an executor that supports the given language.
     *
     * @param language the language identifier
     * @return the executor or null if not found
     */
    private CodeExecutor findExecutor(String language) {
        // First try exact match
        CodeExecutor executor = executors.get(language);
        if (executor != null) {
            return executor;
        }

        // Try to find by supports() method
        for (CodeExecutor exec : executors.values()) {
            if (exec.supports(language)) {
                return exec;
            }
        }

        return null;
    }

    /**
     * Get list of supported languages.
     *
     * @return list of supported language identifiers
     */
    public List<String> getSupportedLanguages() {
        return executors.values().stream()
                .map(CodeExecutor::getLanguage)
                .toList();
    }

    /**
     * Check if a language is supported.
     *
     * @param language the language identifier
     * @return true if supported, false otherwise
     */
    public boolean isLanguageSupported(String language) {
        if (language == null) return false;
        return findExecutor(language.toLowerCase().trim()) != null;
    }
}