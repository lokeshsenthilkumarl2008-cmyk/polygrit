package com.polyglotmesh.resource;

public final class ResourceLimitValidator {

    public void validate(ResourceLimits limits) {
        if (limits == null) {
            throw new IllegalArgumentException("Resource limits must not be null");
        }
        if (limits.timeoutMillis() > 300_000) {
            throw new IllegalArgumentException("Execution timeout exceeds the server maximum of 300 seconds");
        }
        if (limits.guestHeapBytes() > 512 * 1024 * 1024) {
            throw new IllegalArgumentException("Guest heap exceeds the server maximum of 512 MB");
        }
        if (limits.maxOutputBytes() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Output limit exceeds the server maximum of 10 MB");
        }
        if (limits.maxThreads() > 16) {
            throw new IllegalArgumentException("Guest thread limit exceeds the server maximum of 16");
        }
    }
}
