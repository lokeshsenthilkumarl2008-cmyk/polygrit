package com.polyglotmesh.execution.executor;

import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class JavaExecutorTest {

    private JavaExecutor executor;
    private ExecutionConfig config;

    @BeforeEach
    void setUp() {
        config = new ExecutionConfig();
        config.setDefaultTimeoutMs(30000);
        config.setMaxTimeoutMs(120000);
        config.setDefaultMaxOutputSize(1024 * 1024);
        config.setMaxOutputSize(10 * 1024 * 1024);
        config.setEnableSecurityRestrictions(true);
        config.setJavaCompilationTimeoutMs(60000);
        config.setJavaExecutionTimeoutMs(30000);

        executor = new JavaExecutor(config);
    }

    @Test
    @Timeout(10)
    void testGetLanguage() {
        assertEquals("java", executor.getLanguage());
    }

    @Test
    void testSupports() {
        assertTrue(executor.supports("java"));
        assertTrue(executor.supports("Java"));
        assertTrue(executor.supports("JAVA"));
        assertFalse(executor.supports("python"));
        assertFalse(executor.supports("javascript"));
        assertFalse(executor.supports(null));
        assertFalse(executor.supports(""));
    }

    @Test
    @Timeout(30)
    void testHelloWorld() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello from Java\");\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("java", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from Java"));
        assertNull(result.getError());
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertTrue(result.getExecutionTime() > 0);
    }

    @Test
    @Timeout(30)
    void testAddition() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = 10;\n" +
                "        int b = 20;\n" +
                "        System.out.println(a + b);\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("30"));
    }

    @Test
    @Timeout(30)
    void testLoop() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        for (int i = 0; i < 5; i++) {\n" +
                "            System.out.println(i);\n" +
                "        }\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("0"));
        assertTrue(result.getOutput().contains("4"));
    }

    @Test
    @Timeout(30)
    void testDivisionByZero() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = 10;\n" +
                "        int b = 0;\n" +
                "        System.out.println(a / b);\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().toLowerCase().contains("arithmeticexception") ||
                   result.getError().toLowerCase().contains("divide by zero"));
    }

    @Test
    @Timeout(30)
    void testCompilationError() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Missing semicolon\")\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.getStatus());
        assertTrue(result.getError().toLowerCase().contains("error") ||
                   result.getError().toLowerCase().contains(";"));
    }

    @Test
    @Timeout(30)
    void testMissingMainMethod() {
        String code = "public class Main {\n" +
                "    public void someMethod() {\n" +
                "        System.out.println(\"No main\");\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        // Should fail at execution time with NoSuchMethodError or similar
        assertTrue(result.getStatus() == ExecutionStatus.FAILED ||
                   result.getStatus() == ExecutionStatus.COMPILATION_ERROR);
    }

    @Test
    @Timeout(10)
    void testEmptyCode() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
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
                .language("java")
                .code(null)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(30)
    void testMultiplePrintStatements() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"First\");\n" +
                "        System.out.println(\"Second\");\n" +
                "        System.out.println(\"Third\");\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("First"));
        assertTrue(result.getOutput().contains("Second"));
        assertTrue(result.getOutput().contains("Third"));
    }

    @Test
    @Timeout(30)
    void testSystemErr() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.err.println(\"Error output\");\n" +
                "        System.out.println(\"Normal output\");\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertTrue(result.isSuccess());
        // Both stdout and stderr are captured together
        assertTrue(result.getOutput().contains("Error output"));
        assertTrue(result.getOutput().contains("Normal output"));
    }

    @Test
    @Timeout(10)
    void testInvalidClassName() {
        // Code without a proper class declaration
        String code = "System.out.println(\"No class\");";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = executor.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.getStatus());
    }
}