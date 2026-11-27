package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class JavaScriptExecutorTest {

    private JavaScriptExecutor executor;
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

        executor = new JavaScriptExecutor(config);
    }

    @Test
    @Timeout(10)
    void testGetLanguage() {
        assertEquals("javascript", executor.getLanguage());
    }

    @Test
    void testSupports() {
        assertTrue(executor.supports("javascript"));
        assertTrue(executor.supports("JavaScript"));
        assertTrue(executor.supports("JAVASCRIPT"));
        assertTrue(executor.supports("js"));
        assertTrue(executor.supports("JS"));
        assertFalse(executor.supports("python"));
        assertFalse(executor.supports("java"));
        assertFalse(executor.supports(null));
        assertFalse(executor.supports(""));
    }

    @Test
    @Timeout(15)
    void testHelloWorld() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("console.log('Hello from JavaScript');")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("javascript", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from JavaScript"));
        assertNull(result.getError());
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertTrue(result.getExecutionTime() > 0);
    }

    @Test
    @Timeout(15)
    void testAddition() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("let a = 10;\nlet b = 20;\nconsole.log(a + b);")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("30"));
    }

    @Test
    @Timeout(15)
    void testLoop() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("for (let i = 0; i < 5; i++) {\n    console.log(i);\n}")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("0"));
        assertTrue(result.getOutput().contains("4"));
    }

    @Test
    @Timeout(15)
    void testUndefinedVariable() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("console.log(undefinedVariable);")
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().toLowerCase().contains("referenceerror") ||
                   result.getError().toLowerCase().contains("undefined"));
    }

    @Test
    @Timeout(10)
    void testSyntaxError() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("console.log('Hello';") // Missing closing parenthesis
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
                .language("javascript")
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
                .language("javascript")
                .code(null)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(15)
    void testMultipleConsoleLog() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("console.log('First');\nconsole.log('Second');\nconsole.log('Third');")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("First"));
        assertTrue(result.getOutput().contains("Second"));
        assertTrue(result.getOutput().contains("Third"));
    }

    @Test
    @Timeout(15)
    void testJsAlias() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("js")
                .code("console.log('Hello from js alias');")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("Hello from js alias"));
    }

    @Test
    @Timeout(15)
    void testPrintFunction() {
        // GraalJS also supports print() function
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("print('Hello from print');")
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("Hello from print"));
    }
}