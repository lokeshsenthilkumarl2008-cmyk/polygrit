package com.polyglotmesh.docker;

public record ContainerConfiguration(
        String image,
        long memoryBytes,
        long cpuNanoCores,
        boolean networkEnabled,
        boolean readOnlyRootFilesystem) {

    public static ContainerConfiguration defaults() {
        return new ContainerConfiguration("alpine:3.21", 256 * 1024 * 1024, 500_000_000, false, true);
    }

    public ContainerConfiguration {
        if (image == null || image.isBlank()) {
            throw new IllegalArgumentException("Container image must not be blank");
        }
        if (memoryBytes < 0) {
            throw new IllegalArgumentException("Container memory must not be negative");
        }
        if (cpuNanoCores < 0) {
            throw new IllegalArgumentException("Container CPU quota must not be negative");
        }
    }
}
