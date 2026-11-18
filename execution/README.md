# PolyglotMesh Execution Engine

## Overview

The PolyglotMesh Execution Engine is a GraalVM-based multi-language runtime sandbox that enables secure execution of Java, Python, and JavaScript code within a Spring Boot application. It provides a unified API for executing code in different languages while maintaining isolation, security, and resource limits.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Execution Service                        │
├─────────────────────────────────────────────────────────────┤
│                  PolyglotExecutionEngine                    │
├──────────────┬──────────────────┬──────────────────────────┤
│ PythonExecutor│ JavaScriptExecutor│ JavaExecutor             │
├──────────────┼──────────────────┼──────────────────────────┤
│  GraalPy     │    GraalJS       │ Java Compiler API +      │
│  (Polyglot)  │   (Polyglot)     │ Separate Process         │
└──────────────┴──────────────────┴──────────────────────────┘
```

## GraalVM Configuration

### Dependencies

The engine uses the following GraalVM components (version 24.1.2):

| Component | Artifact | Purpose |
|-----------|----------|---------|
| GraalVM SDK | `org.graalvm.sdk:graal-sdk` | Core Polyglot API |
| GraalJS | `org.graalvm.js:js` | JavaScript execution |
| GraalPy | `org.graalvm.python:python` | Python execution |
| Truffle API | `org.graalvm.truffle:truffle-api` | Polyglot context infrastructure |

### Version Compatibility

- **Java Version**: 17 (LTS)
- **GraalVM Version**: 24.1.2
- **Spring Boot**: 3.5.6

All GraalVM components use the same version to ensure compatibility.

## Python Execution (GraalPy)

### How It Works

1. Creates an isolated `Context` for each execution
2. Configures security restrictions (no host access, no IO, no native access)
3. Executes code using `context.eval(Source.newBuilder("python", code))`
4. Captures stdout/stderr via custom `PrintStream` wrappers
5. Measures execution time
6. Forcefully closes context on timeout

### Example

```python
print("Hello from Python")

a = 10
b = 20
print(a + b)

for i in range(5):
    print(i)
```

### Error Handling

- **Syntax Errors**: Caught via `PolyglotException.isSyntaxError()`
- **Runtime Errors**: Extracted from guest language exception (e.g., `ZeroDivisionError`)
- **Timeouts**: Thread interruption + forced context closure

## JavaScript Execution (GraalJS)

### How It Works

1. Creates an isolated `Context` for each execution
2. Configures security restrictions
3. Executes code using `context.eval(Source.newBuilder("js", code))`
4. Captures `console.log` and `print` output
5. Measures execution time
4. Forcefully closes context on timeout

### Example

```javascript
console.log("Hello from JavaScript");

let a = 10;
let b = 20;
console.log(a + b);

for (let i = 0; i < 5; i++) {
    console.log(i);
}
```

### Error Handling

- **Syntax Errors**: Caught via `PolyglotException.isSyntaxError()`
- **Runtime Errors**: Extracted from guest language exception (e.g., `ReferenceError`)
- **Timeouts**: Thread interruption + forced context closure

## Java Execution

### How It Works

Java execution is fundamentally different because Java is the host language. The engine:

1. **Extracts class name** from source code using regex
2. **Writes source** to a temporary directory (`Main.java`)
3. **Compiles** using `javac` subprocess with timeout
4. **Executes** using `java` subprocess with timeout
5. **Captures** combined stdout/stderr
6. **Cleans up** temporary files

### Required Source Format

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello from Java");
    }
}
```

**Requirements:**
- Single public class with `main` method
- Class name must match filename (Main.java → public class Main)
- Package declaration optional but must match directory structure if present

### Example

```java
public class Main {
    public static void main(String[] args) {
        int a = 10;
        int b = 20;
        System.out.println(a + b);
    }
}
```

### Error Handling

- **Compilation Errors**: Captured from `javac` output (exit code ≠ 0)
- **Runtime Errors**: Captured from `java` process output (exit code ≠ 0)
- **Timeouts**: Process destruction via `destroyForcibly()`

## API Request Format

### POST /api/execute

```json
{
  "language": "python",
  "code": "print('Hello World')",
  "timeoutMs": 30000,
  "maxOutputSize": 1048576
}
```

### Supported Languages

| Language | Identifiers |
|----------|-------------|
| Python | `python`, `py` |
| JavaScript | `javascript`, `js` |
| Java | `java` |

### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| language | string | Yes | Programming language identifier |
| code | string | Yes | Source code to execute |
| timeoutMs | number | No | Execution timeout in milliseconds (max: 120000) |
| maxOutputSize | number | No | Maximum output size in bytes (max: 10485760) |

## API Response Format

### Success Response

```json
{
  "success": true,
  "language": "python",
  "output": "Hello World\n",
  "error": null,
  "executionTime": 120,
  "status": "COMPLETED"
}
```

### Error Response

```json
{
  "success": false,
  "language": "python",
  "output": "",
  "error": "ZeroDivisionError: division by zero",
  "executionTime": 50,
  "status": "FAILED"
}
```

### Status Values

| Status | Description |
|--------|-------------|
| COMPLETED | Execution succeeded |
| FAILED | Runtime error occurred |
| TIMEOUT | Execution exceeded time limit |
| COMPILATION_ERROR | Java compilation failed |
| INVALID_REQUEST | Empty code, missing language, etc. |
| UNSUPPORTED_LANGUAGE | Language not supported |

## Error Handling

### Principles

1. **Never expose internal stack traces** to frontend users
2. **Preserve useful error messages** (exception type + message)
3. **Handle null/empty input** gracefully
4. **Prevent cascade failures** - one failed execution doesn't affect others
5. **Return structured errors** with appropriate status codes

### Error Categories

| Category | Languages | Handling |
|----------|-----------|----------|
| Syntax Errors | Python, JavaScript | Detected via `PolyglotException.isSyntaxError()` |
| Runtime Errors | All | Extracted from guest language exception |
| Compilation Errors | Java | Captured from `javac` stderr |
| Timeouts | All | Thread/process termination |
| Resource Limits | All | Output truncation with notice |

## Timeout Configuration

### Default Timeouts

| Operation | Default | Maximum |
|-----------|---------|---------|
| Python/JS Execution | 30 seconds | 120 seconds |
| Java Compilation | 60 seconds | 120 seconds |
| Java Execution | 30 seconds | 120 seconds |

### Configuration Properties

```properties
execution.default-timeout-ms=30000
execution.max-timeout-ms=120000
execution.java-compilation-timeout-ms=60000
execution.java-execution-timeout-ms=30000
```

### Timeout Behavior

1. **GraalVM (Python/JS)**: Execution runs in separate thread; context forcefully closed on timeout
2. **Java**: Subprocess destroyed via `destroyForcibly()` on timeout

## Output Limits

### Default Limits

| Limit | Default | Maximum |
|-------|---------|---------|
| Output Size | 1 MB | 10 MB |

### Configuration Properties

```properties
execution.default-max-output-size=1048576
execution.max-output-size=10485760
```

### Behavior

When output exceeds the limit:
- Output is truncated at the limit
- Truncation notice is appended: `... [output truncated, exceeded X bytes]`

## Security

### GraalVM Context Restrictions

The following restrictions are applied by default (configurable via `ExecutionConfig`):

| Restriction | Default | Description |
|-------------|---------|-------------|
| Host Access | Disabled | Prevents access to Java host objects |
| Host Class Lookup | Disabled | Prevents loading arbitrary Java classes |
| Native Access | Disabled | Prevents native library access |
| IO Operations | Disabled | Prevents file/network access |
| Experimental Options | Disabled | Prevents unstable features |

### Java Process Isolation

Java code executes in a **separate OS process** with:
- No special permissions
- Configurable timeouts
- Output size limits
- Automatic cleanup of temporary files

### Limitations

⚠️ **Important**: In-process GraalVM contexts (Python/JavaScript) are **not** complete security boundaries. They provide language-level isolation but share the JVM process. For production use requiring strong isolation:

- Coordinate with Developer 3 for container/process-level isolation
- Consider running executors in separate containers
- Implement memory/CPU limits at OS/container level
- Network/filesystem restrictions should be enforced externally

### Security Integration Points

The engine exposes configuration points for Developer 3 (Security Module):

```java
// ExecutionConfig properties for security integration
execution.enable-security-restrictions=true
execution.allow-host-access=false
execution.allow-host-class-lookup=false
execution.allow-native-access=false
execution.allow-io=false
```

## Running Tests

### Prerequisites

- JDK 17+
- Maven 3.8+
- GraalVM 24.1.2 (or compatible JDK with GraalVM components)

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=PythonExecutorTest
./mvnw test -Dtest=JavaScriptExecutorTest
./mvnw test -Dtest=JavaExecutorTest
./mvnw test -Dtest=PolyglotExecutionEngineTest
```

### Test Coverage

| Test Class | Coverage |
|------------|----------|
| PythonExecutorTest | Hello World, arithmetic, loops, division by zero, syntax errors, empty code, aliases, large output |
| JavaScriptExecutorTest | Hello World, arithmetic, loops, undefined variable, syntax errors, empty code, aliases, print function |
| JavaExecutorTest | Hello World, arithmetic, loops, division by zero, compilation errors, missing main, empty code, stderr capture |
| PolyglotExecutionEngineTest | Language routing, unsupported languages, null requests, aliases, repeated executions, mixed languages |

## Known Limitations

1. **Java Class Name**: Must be `Main` (or match the filename). Dynamic class name detection is basic.
2. **GraalPy Startup**: First execution has higher latency due to context initialization.
3. **In-Process Isolation**: Python/JavaScript share JVM memory; not suitable for untrusted code without containerization.
4. **Java Security**: No SecurityManager (removed in Java 17); relies on process isolation.
5. **Resource Limits**: No built-in memory/CPU limits for GraalVM contexts (requires OS/container level).
6. **Polyglot Context Reuse**: Each execution creates a new context (by design for isolation).
7. **Large Output**: Truncation may cut mid-line; no streaming/chunked response support.
8. **Blocking I/O**: Execution is synchronous; consider async wrapper for high-throughput scenarios.

## Building

```bash
./mvnw clean compile
./mvnw clean package
```

## Integration

See [INTEGRATION_NOTES.md](INTEGRATION_NOTES.md) for integration instructions for other developers.