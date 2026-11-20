package com.polyglotmesh.resource;

public record ResourceUsage(
        Long guestMemoryBytes,
        Long containerCpuNanoCores,
        Long containerMemoryBytes) {
}
