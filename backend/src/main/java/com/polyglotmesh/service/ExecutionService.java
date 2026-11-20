
package com.polyglotmesh.service;

import java.util.Set;
import java.util.Locale;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polyglotmesh.dto.ExecutionRequest;
import com.polyglotmesh.dto.ExecutionResponse;
import com.polyglotmesh.entity.ExecutionHistory;
import com.polyglotmesh.repository.ExecutionHistoryRepository;
import com.polyglotmesh.exception.ResourceNotFoundException;

@Service
public class ExecutionService {

    private static final Set<String> SUPPORTED_LANGUAGES =
            Set.of("python", "javascript", "ruby");

    private final ExecutionHistoryRepository historyRepository;
    private final ExecutionEngine executionEngine;

    // Constructor Injection
    public ExecutionService(
            ExecutionHistoryRepository historyRepository,
            ExecutionEngine executionEngine) {

        this.historyRepository = historyRepository;
        this.executionEngine = executionEngine;
    }

    // Test Service
    public String testService() {
        return "Execution Service is Working!";
    }

    // Fetch all execution history
    public List<ExecutionHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    // Fetch history by ID
    public ExecutionHistory getHistoryById(String id) {

        return historyRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Execution history not found: " + id
                    )
                );
    }

    // Delete history by ID
    public void deleteHistory(String id) {

        if (!historyRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Execution history not found: " + id
            );
        }

        historyRepository.deleteById(id);
    }

    // Execute Code
    public ExecutionResponse executeCode(ExecutionRequest request) {

        String language = request.getLanguage()
                .trim()
                .toLowerCase(Locale.ROOT);

        String code = request.getCode();

        // Create execution history
        ExecutionHistory history = new ExecutionHistory();
        history.setLanguage(language);
        history.setCode(code);

        // Validate supported language
        if (!SUPPORTED_LANGUAGES.contains(language)) {

            history.setStatus("FAILED");
            history.setError(
                "Unsupported language. Supported languages: Python, JavaScript, Ruby."
            );

            historyRepository.save(history);

            return new ExecutionResponse(
                null,
                "FAILED",
                history.getError()
            );
        }

        // Measure execution time
        long startTime = System.nanoTime();

        try {

            // Call Execution Engine
            ExecutionResponse response =
                    executionEngine.execute(language, code);

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            // Update execution history
            history.setOutput(response.getOutput());
            history.setStatus(response.getStatus());
            history.setError(response.getError());
            history.setExecutionTime(executionTime);

            // Save history in MongoDB
            historyRepository.save(history);

            return response;

        } catch (Exception ex) {

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            // Handle execution failure
            history.setStatus("FAILED");
            history.setError(
                "Execution failed due to an internal error."
            );
            history.setExecutionTime(executionTime);

            // Save failed execution history
            historyRepository.save(history);

            return new ExecutionResponse(
                null,
                "FAILED",
                "Execution failed due to an internal error."
            );
        }
    }
}
