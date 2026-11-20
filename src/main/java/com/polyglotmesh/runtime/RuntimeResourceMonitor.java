package com.polyglotmesh.runtime;

import com.polyglotmesh.resource.ResourceUsage;
import java.time.Instant;

public final class RuntimeResourceMonitor {

    private Instant startTime;
    private Instant endTime;
    private long startNanos;
    private long endNanos;

    public void start() {
        startNanos = System.nanoTime();
        startTime = Instant.now();
    }

    public long elapsedNanos() {
        return System.nanoTime() - startNanos;
    }

    public void stop() {
        endNanos = System.nanoTime();
        endTime = Instant.now();
    }

    public long durationNanos() {
        long end = endNanos == 0 ? System.nanoTime() : endNanos;
        return end - startNanos;
    }

    public Instant startTime() {
        return startTime;
    }

    public Instant endTime() {
        return endTime;
    }

    public ResourceUsage usage() {
        return new ResourceUsage(null, null, null);
    }

    public ResourceUsage usage(ResourceUsage usage) {
        return usage == null ? usage() : usage;
    }
}
