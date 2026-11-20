package com.polyglotmesh.resource;

import java.util.Objects;

public record ResourceLimits(
        long timeoutMillis,
        long guestHeapBytes,
        long maxOutputBytes,
        int maxThreads,
        long dockerMemoryBytes,
        long dockerCpuNanoCores) {

    public ResourceLimits {
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis must be greater than zero");
        }
        if (guestHeapBytes < 0) {
            throw new IllegalArgumentException("guestHeapBytes must not be negative");
        }
        if (maxOutputBytes <= 0) {
            throw new IllegalArgumentException("maxOutputBytes must be greater than zero");
        }
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("maxThreads must be greater than zero");
        }
        if (dockerMemoryBytes < 0) {
            throw new IllegalArgumentException("dockerMemoryBytes must not be negative");
        }
        if (dockerCpuNanoCores < 0) {
            throw new IllegalArgumentException("dockerCpuNanoCores must not be negative");
        }
    }

    public static ResourceLimits defaults() {
        return new ResourceLimits(
                5_000,
                128 * 1024 * 1024,
                1024 * 1024,
                4,
                256 * 1024 * 1024,
                500_000_000);
    }

    public static ResourceLimits fromEnvironment() {
        return new ResourceLimits(
                positiveLong("POLYGLOTMESH_TIMEOUT_MILLIS", defaults().timeoutMillis()),
                positiveLong("POLYGLOTMESH_GUEST_HEAP_BYTES", defaults().guestHeapBytes()),
                positiveLong("POLYGLOTMESH_MAX_OUTPUT_BYTES", defaults().maxOutputBytes()),
                (int) positiveLong("POLYGLOTMESH_MAX_THREADS", defaults().maxThreads()),
                nonNegativeLong("POLYGLOTMESH_DOCKER_MEMORY_BYTES", defaults().dockerMemoryBytes()),
                nonNegativeLong("POLYGLOTMESH_DOCKER_CPU_NANO_CORES", defaults().dockerCpuNanoCores()));
    }

    private static long positiveLong(String name, long defaultValue) {
        long value = nonNegativeLong(name, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private static long nonNegativeLong(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed >= 0 ? parsed : defaultValue;
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
