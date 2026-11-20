package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.resource.ResourceLimits;
import com.polyglotmesh.runtime.ExecutionManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxSecurityTest {

    @Test
    void normalJavaScriptExecution() {
        try (ExecutionManager manager = new ExecutionManager()) {
            ExecutionResult result = manager.execute(
                    new ExecutionRequest("js", "40 + 2"), false);

            assertEquals(ExecutionStatus.COMPLETED, result.status());
            assertEquals("42\n", result.output());
        }
    }

    @Test
    void infiniteLoopTimesOut() {
        ResourceLimits limits = new ResourceLimits(
                250, 64 * 1024 * 1024, 1024 * 1024, 4, 0, 0);
        try (ExecutionManager manager = new ExecutionManager()) {
            ExecutionResult result = manager.execute(
                    new ExecutionRequest("js", "while (true) {}", limits), false);

            assertEquals(ExecutionStatus.TIMEOUT, result.status());
            assertTrue(result.timeout());
            assertTrue(result.errorMessage().contains("timeout"));
        }
    }

    @Test
    void unauthorizedJavaHostAccessIsDenied() {
        try (ExecutionManager manager = new ExecutionManager()) {
            ExecutionResult result = manager.execute(
                    new ExecutionRequest("js",
                            "Java.type('java.lang.System').exit(0)"), false);

            assertTrue(result.status() == ExecutionStatus.SECURITY_VIOLATION
                    || result.status() == ExecutionStatus.FAILED);
        }
    }

    @Test
    void concurrentExecutionsHaveIndependentResults() throws Exception {
        try (ExecutionManager manager = new ExecutionManager()) {
            java.util.concurrent.Future<ExecutionResult> one =
                    java.util.concurrent.Executors.newSingleThreadExecutor()
                            .submit(() -> manager.execute(
                                    new ExecutionRequest("js", "21 * 2"), false));
            java.util.concurrent.Future<ExecutionResult> two =
                    java.util.concurrent.Executors.newSingleThreadExecutor()
                            .submit(() -> manager.execute(
                                    new ExecutionRequest("js", "20 + 22"), false));

            assertEquals("42\n", one.get().output());
            assertEquals("42\n", two.get().output());
        }
    }
}
