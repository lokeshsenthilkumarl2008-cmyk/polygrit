# PolyglotMesh Execution Engine - Integration Notes

## Files Created

### Core Engine
| File | Description |
|------|-------------|
| `src/main/java/com/polyglotmesh/PolyglotMeshApplication.java` | Spring Boot main application |
| `src/main/java/com/polyglotmesh/execution/config/ExecutionConfig.java` | Configuration properties |
| `src/main/java/com/polyglotmesh/execution/engine/PolyglotExecutionEngine.java` | Main execution engine (routes to executors) |
| `src/main/java/com/polyglotmesh/execution/engine/ExecutionService.java` | Service layer for REST controller |

### Executors
| File | Description |
|------|-------------|
| `src/main/java/com/polyglotmesh/execution/executor/CodeExecutor.java` | Executor interface |
| `src/main/java/com/polyglotmesh/execution/executor/PythonExecutor.java` | Python execution via GraalPy |
| `src/main/java/com/polyglotmesh/execution/executor/JavaScriptExecutor.java` | JavaScript execution via GraalJS |
| `src/main/java/com/polyglotmesh/execution/executor/JavaExecutor.java` | Java execution via Compiler API + subprocess |

### Models
| File | Description |
|------|-------------|
| `src/main/java/com/polyglotmesh/execution/model/ExecutionRequest.java` | Request DTO |
| `src/main/java/com/polyglotmesh/execution/model/ExecutionResult.java` | Response DTO |
| `src/main/java/com/polyglotmesh/execution/model/ExecutionStatus.java` | Status enum |

### Exceptions
| File | Description |
|------|-------------|
| `src/main/java/com/polyglotmesh/execution/exception/ExecutionException.java` | Base execution exception |
| `src/main/java/com/polyglotmesh/execution/exception/UnsupportedLanguageException.java` | Unsupported language exception |

### Controller
| File | Description |
|------|-------------|
| `src/main/java/com/polyglotmesh/execution/controller/ExecutionController.java` | REST endpoints |

### Configuration
| File | Description |
|------|-------------|
| `src/main/resources/application.properties` | Application configuration |
| `pom.xml` | Maven build with GraalVM dependencies |

### Tests
| File | Description |
|------|-------------|
| `src/test/java/com/polyglotmesh/execution/executor/PythonExecutorTest.java` | Python executor tests |
| `src/test/java/com/polyglotmesh/execution/executor/JavaScriptExecutorTest.java` | JavaScript executor tests |
| `src/test/java/com/polyglotmesh/execution/executor/JavaExecutorTest.java` | Java executor tests |
| `src/test/java/com/polyglotmesh/execution/engine/PolyglotExecutionEngineTest.java` | Engine integration tests |

### Documentation
| File | Description |
|------|-------------|
| `execution/README.md` | Complete documentation |
| `execution/INTEGRATION_NOTES.md` | This file |

---

## Files Modified

None (new project created from scratch).

---

## Public Service Methods

### ExecutionService

```java
// Main execution method
ExecutionResult execute(ExecutionRequest request)

// Convenience methods
ExecutionResult execute(String language, String code)
ExecutionResult execute(String language, String code, long timeoutMs)

// Utility methods
List<String> getSupportedLanguages()
boolean isLanguageSupported(String language)
```

### PolyglotExecutionEngine

```java
// Main execution method
ExecutionResult execute(ExecutionRequest request)

// Utility methods
List<String> getSupportedLanguages()
boolean isLanguageSupported(String language)
```

---

## Required Dependencies

### GraalVM Dependencies (pom.xml)

```xml
<!-- GraalVM SDK (Core Polyglot API) -->
<dependency>
    <groupId>org.graalvm.sdk</groupId>
    <artifactId>graal-sdk</artifactId>
</dependency>

<!-- GraalJS for JavaScript execution -->
<dependency>
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>24.1.2</version>
</dependency>

<!-- GraalPy for Python execution -->
<dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python</artifactId>
    <version>24.1.2</version>
</dependency>

<!-- GraalVM Truffle API -->
<dependency>
    <groupId>org.graalvm.truffle</groupId>
    <artifactId>truffle-api</artifactId>
    <version>24.1.2</version>
</dependency>
```

### Spring Boot Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Other Dependencies

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
```

---

## DTO Structures

### ExecutionRequest

```json
{
  "language": "python",
  "code": "print('Hello World')",
  "timeoutMs": 30000,
  "maxOutputSize": 1048576
}
```

| Field | Type | Required | Default | Max |
|-------|------|----------|---------|-----|
| language | string | Yes | - | - |
| code | string | Yes | - | - |
| timeoutMs | number | No | 30000 | 120000 |
| maxOutputSize | number | No | 1048576 | 10485760 |

### ExecutionResult

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

| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Execution succeeded |
| language | string | Language executed |
| output | string | Standard output |
| error | string | Standard error (null if success) |
| executionTime | number | Execution time in milliseconds |
| status | string | Execution status enum |

### ExecutionStatus Values

- `COMPLETED` - Success
- `FAILED` - Runtime error
- `TIMEOUT` - Execution timeout
- `COMPILATION_ERROR` - Java compilation failed
- `INVALID_REQUEST` - Invalid input
- `UNSUPPORTED_LANGUAGE` - Language not supported

---

## Integration Instructions for Developer 1 (Backend/API)

### 1. Add Dependency

The execution engine is part of the same Spring Boot application. No separate dependency needed.

### 2. Controller is Ready

The `ExecutionController` exposes:
- `POST /api/execute` - Main execution endpoint
- `POST /api/execute/simple` - Simple parameter endpoint
- `GET /api/execute/languages` - List supported languages
- `GET /api/execute/languages/{language}` - Check language support

### 3. Request/Response Format

Use the DTOs defined above. The controller handles validation automatically via `@Valid`.

### 4. Example Integration

```java
@Autowired
private ExecutionService executionService;

// In your controller or service
ExecutionResult result = executionService.execute("python", "print('Hello')");
```

---

## Security Integration Requirements for Developer 3

### Configuration Points

The `ExecutionConfig` class exposes these security-relevant properties:

```java
@ConfigurationProperties(prefix = "execution")
public class ExecutionConfig {
    // Security restrictions
    boolean enableSecurityRestrictions = true;
    boolean allowHostAccess = false;
    boolean allowHostClassLookup = false;
    boolean allowNativeAccess = false;
    boolean allowIO = false;
    boolean allowExperimentalOptions = false;
    
    // Resource limits
    long defaultTimeoutMs = 30000;
    long maxTimeoutMs = 120000;
    long defaultMaxOutputSize = 1048576;
    long maxOutputSize = 10485760;
    
    // Java-specific
    long javaCompilationTimeoutMs = 60000;
    long javaExecutionTimeoutMs = 30000;
    String javaTempDir;
}
```

### Security Recommendations

1. **Keep defaults** - All host access disabled by default
2. **Configure via application.properties** - Don't hardcode
3. **Container isolation** - For production, run each executor in separate containers
4. **Resource limits** - Set memory/CPU limits at container/OS level
5. **Network policies** - Block outbound network access for executor processes
6. **Filesystem** - Use read-only filesystems, mount tmpfs for temp directories

### Integration Points

Developer 3 can:
1. Override `ExecutionConfig` bean for custom security policies
2. Wrap `ExecutionService` with additional validation
3. Implement rate limiting at API gateway level
4. Add audit logging for execution requests

---

## Frontend Response Contract for Developer 4

### Success Response

```typescript
interface ExecutionSuccessResponse {
  success: true;
  language: "python" | "javascript" | "java";
  output: string;
  error: null;
  executionTime: number;  // milliseconds
  status: "COMPLETED";
}
```

### Error Response

```typescript
interface ExecutionErrorResponse {
  success: false;
  language: string;
  output: "";
  error: string;  // Human-readable error message
  executionTime: number;
  status: "FAILED" | "TIMEOUT" | "COMPILATION_ERROR" | "INVALID_REQUEST" | "UNSUPPORTED_LANGUAGE";
}
```

### Union Type

```typescript
type ExecutionResponse = ExecutionSuccessResponse | ExecutionErrorResponse;
```

### Supported Languages Endpoint

```typescript
interface LanguagesResponse {
  languages: ("python" | "javascript" | "java")[];
  count: number;
}
```

### Language Check Endpoint

```typescript
interface LanguageSupportResponse {
  language: string;
  supported: boolean;
}
```

### Request Payload

```typescript
interface ExecutionRequest {
  language: "python" | "py" | "javascript" | "js" | "java";
  code: string;
  timeoutMs?: number;        // Optional, max 120000
  maxOutputSize?: number;    // Optional, max 10485760
}
```

### Error Display Guidelines

1. **COMPLETED** - Show output in green/success style
2. **FAILED** - Show error in red/error style (user code error)
3. **TIMEOUT** - Show "Execution timed out" warning
4. **COMPILATION_ERROR** - Show compilation errors (Java only)
5. **INVALID_REQUEST** - Show validation error (empty code, etc.)
6. **UNSUPPORTED_LANGUAGE** - Show "Language not supported"

### Example Frontend Usage

```javascript
// Execute code
const response = await fetch('/api/execute', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    language: 'python',
    code: 'print("Hello World")'
  })
});

const result = await response.json();

if (result.success) {
  displayOutput(result.output);
} else {
  displayError(result.error, result.status);
}
```

---

## Building and Running

```bash
# Build
./mvnw clean compile

# Run tests
./mvnw test

# Package
./mvnw clean package

# Run application
./mvnw spring-boot:run
```

Application starts on `http://localhost:8080/api`

---

## Verification Checklist

- [ ] GraalVM dependencies configured
- [ ] Python execution works
- [ ] JavaScript execution works
- [ ] Java compilation works
- [ ] Java execution works
- [ ] Standard output captured
- [ ] Standard error captured
- [ ] Syntax errors handled
- [ ] Runtime errors handled
- [ ] Compilation errors handled
- [ ] Execution time measured
- [ ] Execution timeout works
- [ ] Output size restricted
- [ ] Execution contexts isolated
- [ ] Repeated executions work
- [ ] Backend integration contract ready
- [ ] JUnit tests pass
- [ ] Documentation complete
- [ ] Existing project functionality intact