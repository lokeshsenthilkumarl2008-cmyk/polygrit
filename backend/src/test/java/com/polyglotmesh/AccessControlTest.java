package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.runtime.ExecutionManager;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AccessControlTest {

    @Test
    void fileSystemAccessIsBlocked() throws Exception {
        String marker = "polyglotmesh-sandbox-test";
        Path file = Files.createTempFile("polyglotmesh-sandbox-", ".txt");
        try {
            Files.writeString(file, marker);
            String pythonPath = file.toString().replace("\\", "\\\\").replace("'", "\\'");
            try (ExecutionManager manager = new ExecutionManager()) {
                ExecutionResult result = manager.execute(
                        new ExecutionRequest("python", "open('" + pythonPath + "', 'r').read()"),
                        false);

                assertNotEquals(ExecutionStatus.COMPLETED, result.status());
                assertFalse(result.output().contains(marker));
            }
        } finally {
            Files.deleteIfExists(file);
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
