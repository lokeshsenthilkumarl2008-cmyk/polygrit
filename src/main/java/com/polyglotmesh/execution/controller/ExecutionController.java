package com.polyglotmesh.execution.controller;

import com.polyglotmesh.execution.engine.ExecutionService;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for code execution API.
 */
@RestController
@RequestMapping("/api/execute")
public class ExecutionController {

    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    /**
     * Execute code.
     *
     * @param request the execution request
     * @return the execution result
     */
    @PostMapping
    public ResponseEntity<ExecutionResult> execute(@Valid @RequestBody ExecutionRequest request) {
        log.info("Received execution request for language: {}", request.getLanguage());
        ExecutionResult result = executionService.execute(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Execute code with simple parameters (alternative endpoint).
     *
     * @param language the programming language
     * @param code the source code
     * @return the execution result
     */
    @PostMapping("/simple")
    public ResponseEntity<ExecutionResult> executeSimple(
            @RequestParam String language,
            @RequestParam String code) {
        log.info("Received simple execution request for language: {}", language);
        ExecutionResult result = executionService.execute(language, code);
        return ResponseEntity.ok(result);
    }

    /**
     * Get list of supported languages.
     *
     * @return map of supported languages
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        var languages = executionService.getSupportedLanguages();
        return ResponseEntity.ok(Map.of(
                "languages", languages,
                "count", languages.size()
        ));
    }

    /**
     * Check if a language is supported.
     *
     * @param language the language identifier
     * @return support status
     */
    @GetMapping("/languages/{language}")
    public ResponseEntity<Map<String, Object>> isLanguageSupported(@PathVariable String language) {
        boolean supported = executionService.isLanguageSupported(language);
        return ResponseEntity.ok(Map.of(
                "language", language,
                "supported", supported
        ));
    }
}