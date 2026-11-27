package com.polyglotmesh.execution.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for code execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {

    /**
     * The programming language to execute.
     * Supported values: java, python, javascript (and aliases py, js)
     */
    @NotNull(message = "Language is required")
    @NotBlank(message = "Language cannot be blank")
    private String language;

    /**
     * The source code to execute.
     */
    @NotNull(message = "Code is required")
    @NotBlank(message = "Code cannot be blank")
    private String code;

    /**
     * Optional execution timeout in milliseconds.
     * If not provided, default timeout from configuration will be used.
     */
    private Long timeoutMs;

    /**
     * Optional maximum output size in bytes.
     * If not provided, default limit from configuration will be used.
     */
    private Long maxOutputSize;
}