package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class PythonExecutorTest {

    private PythonExecutor executor;
    private ExecutionConfig config;

    @BeforeEach
    void setUp() {
        config = new ExecutionConfig();
        config.setDefaultTimeoutMs(30000);
        config.setMaxTimeoutMs(120000);
        config.setDefaultMaxOutputSize(1024 * 1024);
        config.setMaxOutputSize(10 * 1024 * 1024);
        config.setEnableSecurityRestrictions(true);
        config.setAllowHostAccess(false);
        config.setAllowHostClassLookup(false);
        config.setAllowNativeAccess(false);
        config.setAllowIO(false);
        config.setAllowExperimentalOptions(false);

        executor = new PythonExecutor(config);
    }

    @Test
    @Timeout(10)
    void testGetLanguage() {
        assertEquals("python", executor.getLanguage());
    }

    @Test
    void testSupports() {
        assertTrue(executor.supports("python"));
        assertTrue(executor.supports("Python"));
        assertTrue(executor.supports("PYTHON"));
        assertTrue(executor.supports("py"));
        assertTrue(executor.supports("Py"));
        assertFalse(executor.supports("javascript"));
        assertFalse(executor.supports("java"));
        assertFalse(executor.supports(null));
        assertFalse(executor.supports(""));
    }

    @Test
    @Timeout(15)
    void testHelloWorld() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("print('Hello from Python')")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("python", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from Python"));
        assertNull(result.getError());
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertTrue(result.getExecutionTime() > 0);
    }

    @Test
    @Timeout(15)
    void testAddition() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("a = 10\nb = 20\nprint(a + b)")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("30"));
    }

    @Test
    @Timeout(15)
    void testLoop() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("for i in range(5):\n    print(i)")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("0"));
        assertTrue(result.getOutput().contains("4"));
    }

    @Test
    @Timeout(15)
    void testDivisionByZero() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("print(10 / 0)")
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().contains("ZeroDivisionError"));
    }

    @Test
    @Timeout(10)
    void testSyntaxError() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("print('Hello')\nif True:\nprint('missing indent')")
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().toLowerCase().contains("syntax"));
    }

    @Test
    @Timeout(10)
    void testEmptyCode() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("")
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(10)
    void testNullCode() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code(null)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(15)
    void testMultiplePrintStatements() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("print('First')\nprint('Second')\nprint('Third')")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("First"));
        assertTrue(result.getOutput().contains("Second"));
        assertTrue(result.getOutput().contains("Third"));
    }

    @Test
    @Timeout(15)
    void testPyAlias() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("py")
                .code("print('Hello from py alias')")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("Hello from py alias"));
    }

    @Test
    @Timeout(15)
    void testLargeOutput() {
        // Generate output that exceeds default max output size
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("for i in range(1000):\n    print('x' * 1000)")
                .maxOutputSize(5000L) // Limit to 5KB
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        // Output should be truncated
        assertTrue(result.getOutput().length() <= 5000 + 100); // Some buffer for truncation message
    }
}