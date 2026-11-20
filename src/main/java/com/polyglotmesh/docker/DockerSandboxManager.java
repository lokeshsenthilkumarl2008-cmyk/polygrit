package com.polyglotmesh.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.time.Duration;

public final class DockerSandboxManager implements AutoCloseable {

    private final DockerClient client;

    public DockerSandboxManager() {
        this(createDefaultClient());
    }

    DockerSandboxManager(DockerClient client) {
        this.client = client;
    }

    private static DockerClient createDefaultClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient transport = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(20)
                .connectionTimeout(Duration.ofSeconds(5))
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        return DockerClientImpl.getInstance(config, transport);
    }

    public boolean isAvailable() {
        try {
            client.pingCmd().exec();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String createRestrictedContainer(ContainerConfiguration configuration, String executionId) {
        if (!isAvailable()) {
            throw new IllegalStateException("Docker is unavailable. Docker Desktop or the Docker daemon must be running.");
        }
        try {
            client.pullImageCmd(configuration.image())
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker image pull was interrupted", exception);
        }

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withMemory(configuration.memoryBytes())
                .withMemorySwap(configuration.memoryBytes())
                .withReadonlyRootfs(configuration.readOnlyRootFilesystem());
        if (configuration.cpuNanoCores() > 0) {
            hostConfig.withNanoCPUs(configuration.cpuNanoCores());
        }
        if (!configuration.networkEnabled()) {
            hostConfig.withNetworkMode("none");
        }

        CreateContainerResponse response = client.createContainerCmd(configuration.image())
                .withName("polyglotmesh-" + executionId)
                .withHostConfig(hostConfig)
                .withCmd("sh", "-c", "while true; do sleep 1; done")
                .exec();
        client.startContainerCmd(response.getId()).exec();
        return response.getId();
    }

    public void stopAndRemoveContainer(String containerId) {
        if (containerId == null || containerId.isBlank()) {
            return;
        }
        try {
            client.stopContainerCmd(containerId).withTimeout(2).exec();
        } catch (RuntimeException ignored) {
        }
        try {
            client.removeContainerCmd(containerId).withForce(true).exec();
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to close Docker client", exception);
        }
    }
}
