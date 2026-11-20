package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.runtime.ExecutionManager;
import com.polyglotmesh.security.SecurityViolationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessControlTest {

    @Test
    void fileSystemAccessIsBlocked() {
        try (ExecutionManager manager = new ExecutionManager()) {
            ExecutionResult result = manager.execute(
                    new ExecutionRequest("js",
                            "import('fs').then(m=>m.readFileSync('/etc/passwd'))"),
                    false);

            assertTrue(result.output().isEmpty());
            assertTrue(result.status().name().contains("SECURITY")
                    || result.status().name().contains("FAILED"));
        }
    }

    @Test
    void networkAccessIsBlocked() {
        try (ExecutionManager manager = new ExecutionManager()) {
            ExecutionResult result = manager.execute(
                    new ExecutionRequest("js",
                            "fetch('https://example.com')"), false);

            assertFalse(result.status().name().equals("COMPLETED"));
        }
    }
}
