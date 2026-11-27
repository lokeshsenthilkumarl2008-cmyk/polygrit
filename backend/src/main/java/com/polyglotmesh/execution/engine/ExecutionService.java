package com.polyglotmesh.execution.engine;

import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.service.ExecutionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service layer for code execution.
 * Provides a clean interface for the REST controller.
 */
@Service
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    private final PolyglotExecutionEngine executionEngine;
    private final ExecutionHistoryService historyService;

    public ExecutionService(
            PolyglotExecutionEngine executionEngine,
            ExecutionHistoryService historyService) {
        this.executionEngine = executionEngine;
        this.historyService = historyService;
    }

    /**
     * Execute code and return the result.
     *
     * @param request the execution request
     * @return the execution result
     */
    public ExecutionResult execute(ExecutionRequest request) {
        log.info("Executing code for language: {}", request.getLanguage());
        ExecutionResult result = executionEngine.execute(request);
        historyService.record(request, result);
        return result;
    }

    public String testService() {
        return "Execution Service is Working!";
    }

    /**
     * Execute code with simple parameters.
     *
     * @param language the programming language
     * @param code the source code
     * @return the execution result
     */
    public ExecutionResult execute(String language, String code) {
        ExecutionRequest request = ExecutionRequest.builder()
                .language(language)
                .code(code)
                .build();
        return execute(request);
    }

    /**
     * Execute code with timeout.
     *
     * @param language the programming language
     * @param code the source code
     * @param timeoutMs timeout in milliseconds
     * @return the execution result
     */
    public ExecutionResult execute(String language, String code, long timeoutMs) {
        ExecutionRequest request = ExecutionRequest.builder()
                .language(language)
                .code(code)
                .timeoutMs(timeoutMs)
                .build();
        return execute(request);
    }

    /**
     * Get list of supported languages.
     *
     * @return list of supported language identifiers
     */
    public java.util.List<String> getSupportedLanguages() {
        return executionEngine.getSupportedLanguages();
    }

    /**
     * Check if a language is supported.
     *
     * @param language the language identifier
     * @return true if supported, false otherwise
     */
    public boolean isLanguageSupported(String language) {
        return executionEngine.isLanguageSupported(language);
    }
}