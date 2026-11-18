package com.polyglotmesh.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model for code execution.
 * Contains execution output, errors, timing, and status information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    /**
     * Whether the execution was successful.
     */
    private boolean success;

    /**
     * The programming language that was executed.
     */
    private String language;

    /**
     * Standard output from the execution.
     */
    private String output;

    /**
     * Standard error from the execution (if any).
     */
    private String error;

    /**
     * Execution time in milliseconds.
     */
    private long executionTime;

    /**
     * Execution status.
     */
    private ExecutionStatus status;

    /**
     * Create a successful result.
     */
    public static ExecutionResult success(String language, String output, long executionTime) {
        return ExecutionResult.builder()
                .success(true)
                .language(language)
                .output(output)
                .error(null)
                .executionTime(executionTime)
                .status(ExecutionStatus.COMPLETED)
                .build();
    }

    /**
     * Create a failed result.
     */
    public static ExecutionResult failed(String language, String error, long executionTime, ExecutionStatus status) {
        return ExecutionResult.builder()
                .success(false)
                .language(language)
                .output("")
                .error(error)
                .executionTime(executionTime)
                .status(status)
                .build();
    }

    /**
     * Create a timeout result.
     */
    public static ExecutionResult timeout(String language, long executionTime) {
        return ExecutionResult.builder()
                .success(false)
                .language(language)
                .output("")
                .error("Execution timed out")
                .executionTime(executionTime)
                .status(ExecutionStatus.TIMEOUT)
                .build();
    }

    /**
     * Create an invalid request result.
     */
    public static ExecutionResult invalidRequest(String language, String error) {
        return ExecutionResult.builder()
                .success(false)
                .language(language)
                .output("")
                .error(error)
                .executionTime(0)
                .status(ExecutionStatus.INVALID_REQUEST)
                .build();
    }

    /**
     * Create an unsupported language result.
     */
    public static ExecutionResult unsupportedLanguage(String language) {
        return ExecutionResult.builder()
                .success(false)
                .language(language)
                .output("")
                .error("Unsupported language: " + language)
                .executionTime(0)
                .status(ExecutionStatus.UNSUPPORTED_LANGUAGE)
                .build();
    }
}