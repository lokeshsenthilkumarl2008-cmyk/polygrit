package com.polyglotmesh.execution.engine;

import com.polyglotmesh.execution.executor.CodeExecutor;
import com.polyglotmesh.execution.executor.JavaExecutor;
import com.polyglotmesh.execution.executor.JavaScriptExecutor;
import com.polyglotmesh.execution.executor.PythonExecutor;
import com.polyglotmesh.execution.config.ExecutionConfig;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.execution.model.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolyglotExecutionEngineTest {

    private PolyglotExecutionEngine engine;
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
        config.setJavaCompilationTimeoutMs(60000);
        config.setJavaExecutionTimeoutMs(30000);

        PythonExecutor pythonExecutor = new PythonExecutor(config);
        JavaScriptExecutor jsExecutor = new JavaScriptExecutor(config);
        JavaExecutor javaExecutor = new JavaExecutor(config);

        engine = new PolyglotExecutionEngine(List.of(pythonExecutor, jsExecutor, javaExecutor));
    }

    @Test
    void testGetSupportedLanguages() {
        List<String> languages = engine.getSupportedLanguages();
        assertEquals(3, languages.size());
        assertTrue(languages.contains("python"));
        assertTrue(languages.contains("javascript"));
        assertTrue(languages.contains("java"));
    }

    @Test
    void testIsLanguageSupported() {
        assertTrue(engine.isLanguageSupported("python"));
        assertTrue(engine.isLanguageSupported("Python"));
        assertTrue(engine.isLanguageSupported("PYTHON"));
        assertTrue(engine.isLanguageSupported("py"));
        assertTrue(engine.isLanguageSupported("javascript"));
        assertTrue(engine.isLanguageSupported("js"));
        assertTrue(engine.isLanguageSupported("java"));
        assertFalse(engine.isLanguageSupported("ruby"));
        assertFalse(engine.isLanguageSupported("go"));
        assertFalse(engine.isLanguageSupported(null));
        assertFalse(engine.isLanguageSupported(""));
    }

    @Test
    @Timeout(15)
    void testExecutePython() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("python")
                .code("print('Hello from Python')")
                .build();

        ExecutionResult result = engine.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("python", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from Python"));
    }

    @Test
    @Timeout(15)
    void testExecuteJavaScript() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("javascript")
                .code("console.log('Hello from JavaScript');")
                .build();

        ExecutionResult result = engine.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("javascript", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from JavaScript"));
    }

    @Test
    @Timeout(30)
    void testExecuteJava() {
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello from Java\");\n" +
                "    }\n" +
                "}";

        ExecutionRequest request = ExecutionRequest.builder()
                .language("java")
                .code(code)
                .build();

        ExecutionResult result = engine.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("java", result.getLanguage());
        assertTrue(result.getOutput().contains("Hello from Java"));
    }

    @Test
    @Timeout(10)
    void testExecuteUnsupportedLanguage() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("ruby")
                .code("puts 'Hello'")
                .build();

        ExecutionResult result = engine.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.UNSUPPORTED_LANGUAGE, result.getStatus());
        assertTrue(result.getError().contains("Unsupported language"));
    }

    @Test
    @Timeout(10)
    void testExecuteNullRequest() {
        ExecutionResult result = engine.execute(null);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(10)
    void testExecuteEmptyLanguage() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("")
                .code("print('test')")
                .build();

        ExecutionResult result = engine.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(10)
    void testExecuteNullLanguage() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language(null)
                .code("print('test')")
                .build();

        ExecutionResult result = engine.execute(request);

        assertFalse(result.isSuccess());
        assertEquals(ExecutionStatus.INVALID_REQUEST, result.getStatus());
    }

    @Test
    @Timeout(15)
    void testExecuteWithPyAlias() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("py")
                .code("print('Python alias')")
                .build();

        ExecutionResult result = engine.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("python", result.getLanguage());
        assertTrue(result.getOutput().contains("Python alias"));
    }

    @Test
    @Timeout(15)
    void testExecuteWithJsAlias() {
        ExecutionRequest request = ExecutionRequest.builder()
                .language("js")
                .code("console.log('JavaScript alias');")
                .build();

        ExecutionResult result = engine.execute(request);

        assertTrue(result.isSuccess());
        assertEquals("javascript", result.getLanguage());
        assertTrue(result.getOutput().contains("JavaScript alias"));
    }

    @Test
    @Timeout(30)
    void testRepeatedExecutions() {
        // Run multiple executions to verify isolation
        for (int i = 0; i < 5; i++) {
            ExecutionRequest request = ExecutionRequest.builder()
                    .language("python")
                    .code("print('Execution " + i + "')")
                    .build();

            ExecutionResult result = engine.execute(request);

            assertTrue(result.isSuccess());
            assertTrue(result.getOutput().contains("Execution " + i));
        }
    }

    @Test
    @Timeout(60)
    void testMixedLanguageExecutions() {
        // Test executing different languages in sequence
        String[] languages = {"python", "javascript", "java", "python", "js"};
        String[] codes = {
            "print('Python 1')",
            "console.log('JS 1');",
            "public class Main { public static void main(String[] args) { System.out.println(\"Java 1\"); } }",
            "print('Python 2')",
            "console.log('JS 2');"
        };

        for (int i = 0; i < languages.length; i++) {
            ExecutionRequest request = ExecutionRequest.builder()
                    .language(languages[i])
                    .code(codes[i])
                    .build();

            ExecutionResult result = engine.execute(request);

            assertTrue(result.isSuccess(), "Execution " + i + " failed: " + result.getError());
        }
    }
}