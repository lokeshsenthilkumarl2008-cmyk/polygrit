package com.polyglotmesh.model;

import com.polyglotmesh.security.SecurityViolation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ExecutionResult(
        String executionId,
        String language,
        ExecutionStatus status,
        String output,
        String errorMessage,
        Instant startTime,
        Instant endTime,
        long durationNanos,
        Long guestMemoryBytes,
        Long containerCpuNanoCores,
        Long containerMemoryBytes,
        boolean timeout,
        boolean resourceExhausted,
        List<SecurityViolation> securityViolations) {

    public ExecutionResult {
        securityViolations = Collections.unmodifiableList(
                securityViolations == null ? new ArrayList<>() : new ArrayList<>(securityViolations));
        output = Optional.ofNullable(output).orElse("");
    }
}
