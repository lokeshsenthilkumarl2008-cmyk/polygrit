package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;

/**
 * Interface for language-specific code executors.
 */
public interface CodeExecutor {

    /**
     * Execute the given code and return the result.
     *
     * @param request the execution request containing code and configuration
     * @return the execution result
     */
    ExecutionResult execute(ExecutionRequest request);

    /**
     * Get the language identifier this executor handles.
     *
     * @return the language identifier (e.g., "python", "javascript", "java")
     */
    String getLanguage();

    /**
     * Check if this executor supports the given language identifier.
     *
     * @param language the language identifier to check
     * @return true if supported, false otherwise
     */
    boolean supports(String language);
}