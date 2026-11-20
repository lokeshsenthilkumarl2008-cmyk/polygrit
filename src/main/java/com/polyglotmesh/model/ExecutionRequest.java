package com.polyglotmesh.model;

import com.polyglotmesh.resource.ResourceLimits;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ExecutionRequest(
        String executionId,
        String language,
        String code,
        ResourceLimits resourceLimits,
        Instant submittedAt) {

    public ExecutionRequest {
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(code, "code");
        if (executionId == null || executionId.isBlank()) {
            executionId = UUID.randomUUID().toString();
        }
        if (resourceLimits == null) {
            resourceLimits = ResourceLimits.defaults();
        }
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }

    public ExecutionRequest(String language, String code) {
        this(null, language, code, ResourceLimits.defaults(), null);
    }

    public ExecutionRequest(String language, String code, ResourceLimits resourceLimits) {
        this(null, language, code, resourceLimits, null);
    }
}
