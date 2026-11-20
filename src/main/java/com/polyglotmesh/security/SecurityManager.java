package com.polyglotmesh.security;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.resource.ResourceLimitValidator;
import java.util.Set;

public final class SecurityManager {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("js", "javascript", "python");

    private final ResourceLimitValidator validator;
    private final AccessControlManager accessControlManager;

    public SecurityManager() {
        this(new ResourceLimitValidator(), new AccessControlManager());
    }

    public SecurityManager(ResourceLimitValidator validator, AccessControlManager accessControlManager) {
        this.validator = validator;
        this.accessControlManager = accessControlManager;
    }

    public SandboxConfiguration validate(ExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Execution request must not be null");
        }
        String language = request.language().trim().toLowerCase(java.util.Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + request.language());
        }
        validator.validate(request.resourceLimits());
        return SandboxConfiguration.untrusted();
    }

    public AccessControlManager accessControlManager() {
        return accessControlManager;
    }
}
