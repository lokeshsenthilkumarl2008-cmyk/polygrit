package com.polyglotmesh.execution.exception;

import com.polyglotmesh.execution.model.ExecutionStatus;

/**
 * Exception thrown when an unsupported language is requested.
 */
public class UnsupportedLanguageException extends ExecutionException {

    public UnsupportedLanguageException(String language) {
        super("Unsupported language: " + language, ExecutionStatus.UNSUPPORTED_LANGUAGE);
    }

    public UnsupportedLanguageException(String language, Throwable cause) {
        super("Unsupported language: " + language, ExecutionStatus.UNSUPPORTED_LANGUAGE, cause);
    }
}