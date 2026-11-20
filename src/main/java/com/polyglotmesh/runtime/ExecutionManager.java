package com.polyglotmesh.runtime;

import com.polyglotmesh.docker.ContainerConfiguration;
import com.polyglotmesh.docker.DockerSandboxManager;
import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.security.AccessControlManager;
import com.polyglotmesh.security.SandboxConfiguration;
import com.polyglotmesh.security.SecurityManager;
import com.polyglotmesh.security.SecurityViolation;
import com.polyglotmesh.security.SecurityViolationType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.Value;

public final class ExecutionManager implements AutoCloseable {

    private final SecurityManager securityManager;
    private final ExecutionTimeoutManager timeoutManager;
    private final RuntimeResourceMonitor monitor;
    private final DockerSandboxManager dockerManager;

    public ExecutionManager() {
        this(new SecurityManager(), new ExecutionTimeoutManager(),
                new RuntimeResourceMonitor(), new DockerSandboxManager());
    }

    ExecutionManager(SecurityManager securityManager, ExecutionTimeoutManager timeoutManager,
                     RuntimeResourceMonitor monitor, DockerSandboxManager dockerManager) {
        this.securityManager = securityManager;
        this.timeoutManager = timeoutManager;
        this.monitor = monitor;
        this.dockerManager = dockerManager;
    }

    public ExecutionResult execute(ExecutionRequest request) {
        return execute(request, "true".equalsIgnoreCase(System.getenv("POLYGLOTMESH_DOCKER_ENABLED")));
    }

    public ExecutionResult execute(ExecutionRequest request, boolean dockerEnabled) {
        SandboxConfiguration sandbox;
        try {
            sandbox = securityManager.validate(request);
        } catch (RuntimeException exception) {
            return failedResult(request, exception.getMessage(), ExecutionStatus.FAILED,
                    false, false, List.of());
        }

        monitor.start();
        String containerId = null;
        AtomicReference<Context> contextReference = new AtomicReference<>();
        try {
            if (dockerEnabled) {
                if (!dockerManager.isAvailable()) {
                    throw new IllegalStateException(
                            "Docker is unavailable. Docker Desktop or the Docker daemon must be running.");
                }
                ContainerConfiguration dockerConfig = ContainerConfiguration.defaults();
                containerId = dockerManager.createRestrictedContainer(
                        dockerConfig, request.executionId());
            }

            BoundedOutputStream stdout = new BoundedOutputStream(
                    request.resourceLimits().maxOutputBytes());

            String result = timeoutManager.execute(() -> {
                HostAccess strictHostAccess = HostAccess.UNTRUSTED;
                SandboxPolicy policy = isGraalVm25OrLater()
                        ? SandboxPolicy.UNTRUSTED
                        : SandboxPolicy.CONSTRAINED;
                Context.Builder builder = Context.newBuilder(normalizeLanguage(request.language()))
                        .allowHostAccess(strictHostAccess)
                        .allowHostClassLookup(className -> false)
                        .allowCreateThread(sandbox.allowCreateThread())
                        .allowCreateProcess(sandbox.allowCreateProcess())
                        .allowNativeAccess(sandbox.allowNativeAccess())
                        .allowIO(sandbox.allowIo())
                        .allowExperimentalOptions(true)
                        .sandbox(policy)
                        .out(stdout)
                        .err(stdout);

                if (sandbox.allowEnvironmentAccess()) {
                    builder.allowEnvironmentAccess(org.graalvm.polyglot.EnvironmentAccess.INHERIT);
                } else {
                    builder.allowEnvironmentAccess(org.graalvm.polyglot.EnvironmentAccess.NONE);
                }
                if (policy == SandboxPolicy.UNTRUSTED) {
                    builder.option("engine.MaxIsolateMemory",
                            request.resourceLimits().guestHeapBytes() + "B");
                    builder.option("sandbox.MaxHeapMemory",
                            request.resourceLimits().guestHeapBytes() + "B");
                    builder.option("sandbox.MaxOutputSize",
                            request.resourceLimits().maxOutputBytes() + "B");
                    builder.option("sandbox.MaxThreads",
                            String.valueOf(request.resourceLimits().maxThreads()));
                }

                Context context = builder.build();
                contextReference.set(context);
                try {
                    Value value = context.eval(normalizeLanguage(request.language()), request.code());
                    if (!value.isNull()) {
                        stdout.write(value.toString());
                        stdout.write("\n");
                    }
                    context.close();
                    contextReference.set(null);
                    return stdout.toString();
                } finally {
                    Context active = contextReference.getAndSet(null);
                    if (active != null) {
                        active.close(true);
                    }
                }
            }, request.resourceLimits().timeoutMillis());

            monitor.stop();
            return new ExecutionResult(
                    request.executionId(),
                    request.language(),
                    ExecutionStatus.COMPLETED,
                    result,
                    null,
                    monitor.startTime(),
                    monitor.endTime(),
                    monitor.durationNanos(),
                    null,
                    null,
                    null,
                    false,
                    false,
                    List.of());
        } catch (TimeoutException exception) {
            Context timedOutContext = contextReference.getAndSet(null);
            if (timedOutContext != null) {
                timedOutContext.close(true);
            }
            monitor.stop();
            return new ExecutionResult(
                    request.executionId(), request.language(), ExecutionStatus.TIMEOUT,
                    "", "Execution exceeded the configured timeout of "
                            + request.resourceLimits().timeoutMillis() + " ms",
                    monitor.startTime(), monitor.endTime(), monitor.durationNanos(),
                    null, null, null, true, false, List.of());
        } catch (Throwable throwable) {
            monitor.stop();
            String message = throwable.getMessage();
            if (message == null || message.isBlank()) {
                message = throwable.toString();
            }

            if (isResourceExhausted(throwable, message)) {
                return new ExecutionResult(
                        request.executionId(), request.language(), ExecutionStatus.RESOURCE_EXHAUSTED,
                        "", "Resource limit exceeded: " + message,
                        monitor.startTime(), monitor.endTime(), monitor.durationNanos(),
                        null, null, null, false, true, List.of());
            }

            if (securityManager.accessControlManager().isAccessViolation(throwable)) {
                SecurityViolation violation =
                        securityManager.accessControlManager().classify(throwable);
                List<SecurityViolation> violations = new ArrayList<>();
                violations.add(violation);
                return new ExecutionResult(
                        request.executionId(), request.language(), ExecutionStatus.SECURITY_VIOLATION,
                        "", "Security violation: " + message,
                        monitor.startTime(), monitor.endTime(), monitor.durationNanos(),
                        null, null, null, false, false, violations);
            }

            if (throwable instanceof IllegalStateException
                    && message.toLowerCase(Locale.ROOT).contains("docker")) {
                return failedResult(request, message, ExecutionStatus.FAILED,
                        false, false, List.of());
            }

            return failedResult(request, message, ExecutionStatus.FAILED,
                    false, false, List.of());
        } finally {
            if (containerId != null) {
                dockerManager.stopAndRemoveContainer(containerId);
            }
        }
    }

    private ExecutionResult failedResult(ExecutionRequest request, String message,
                                         ExecutionStatus status, boolean timeout,
                                         boolean resourceExhausted,
                                         List<SecurityViolation> violations) {
        monitor.stop();
        return new ExecutionResult(
                request.executionId(), request.language(), status, "", message,
                monitor.startTime(), monitor.endTime(),
                monitor.startTime() == null ? 0 : monitor.durationNanos(),
                null, null, null, timeout, resourceExhausted, violations);
    }

    private boolean isResourceExhausted(Throwable throwable, String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("out of memory")
                || lower.contains("heap allocation")
                || lower.contains("maximum heap")
                || lower.contains("thread limit")
                || lower.contains("maximum thread")
                || lower.contains("maximum output")
                || lower.contains("output size exceeded")
                || lower.contains("cpu time limit")) {
            return true;
        }
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof OutOfMemoryError
                    || current instanceof StackOverflowError
                    || current instanceof PolyglotException polyglotException
                            && polyglotException.isResourceExhausted()) {
                return true;
            }
        }
        return false;
    }

    private String normalizeLanguage(String language) {
        return switch (language.trim().toLowerCase(Locale.ROOT)) {
            case "javascript" -> "js";
            default -> language.trim().toLowerCase(Locale.ROOT);
        };
    }

    private boolean isGraalVm25OrLater() {
        String javaVersion = System.getProperty("java.specification.version", "0");
        try {
            return Integer.parseInt(javaVersion) >= 25;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @Override
    public void close() {
        timeoutManager.close();
        dockerManager.close();
    }

    private static final class BoundedOutputStream extends OutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final long limit;
        private long written;

        private BoundedOutputStream(long limit) {
            this.limit = limit;
        }

        @Override
        public void write(int value) throws IOException {
            if (written >= limit) {
                throw new IOException("Maximum output size exceeded: " + limit + " bytes");
            }
            buffer.write(value);
            written++;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (written + length > limit) {
                throw new IOException("Maximum output size exceeded: " + limit + " bytes");
            }
            buffer.writeBytes(java.util.Arrays.copyOfRange(bytes, offset, offset + length));
            written += length;
        }

        public void write(String value) throws IOException {
            write(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String toString() {
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
