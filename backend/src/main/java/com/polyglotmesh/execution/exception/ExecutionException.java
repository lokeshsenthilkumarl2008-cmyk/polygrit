package com.polyglotmesh.execution.exception;

import com.polyglotmesh.execution.model.ExecutionStatus;

/**
 * Base exception for execution-related errors.
 */
public class ExecutionException extends RuntimeException {

    private final ExecutionStatus status;

    public ExecutionException(String message, ExecutionStatus status) {
        super(message);
        this.status = status;
    }

    public ExecutionException(String message, ExecutionStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public ExecutionStatus getStatus() {
        return status;
    }
}