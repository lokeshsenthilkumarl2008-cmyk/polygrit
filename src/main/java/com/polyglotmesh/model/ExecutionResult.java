package com.polyglotmesh.model;

import com.polyglotmesh.security.SecurityViolation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;



    public ExecutionResult {
        securityViolations = Collections.unmodifiableList(
                securityViolations == null ? new ArrayList<>() : new ArrayList<>(securityViolations));
        output = Optional.ofNullable(output).orElse("");
    }
}
