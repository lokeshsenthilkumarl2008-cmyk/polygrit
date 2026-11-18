package com.polyglotmesh.execution.model;

/**
 * Execution status enumeration.
 * Represents the possible states of a code execution.
 */
public enum ExecutionStatus {
    /**
     * Execution completed successfully.
     */
    COMPLETED,

    /**
     * Execution failed due to runtime error.
     */
    FAILED,

    /**
     * Execution timed out.
     */
    TIMEOUT,

    /**
     * Compilation error (for Java).
     */
    COMPILATION_ERROR,

    /**
     * Invalid request (empty code, malformed request, etc.).
     */
    INVALID_REQUEST,

    /**
     * Unsupported language requested.
     */
    UNSUPPORTED_LANGUAGE
}