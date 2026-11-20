
package com.polyglotmesh.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.polyglotmesh.service.ExecutionService;
import com.polyglotmesh.dto.ExecutionRequest;
import com.polyglotmesh.dto.ExecutionResponse;
import com.polyglotmesh.entity.ExecutionHistory;

@RestController
@RequestMapping("/api")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping("/test")
    public String test() {
        return "PolyglotMesh Backend is Running!";
    }

    @GetMapping("/service-test")
    public String testService() {
        return executionService.testService();
    }

    // Execute code
    @PostMapping("/execute")
    public ExecutionResponse executeCode(
            @Valid @RequestBody ExecutionRequest request) {

        return executionService.executeCode(request);
    }

    // Fetch all execution history
    @GetMapping("/history")
    public List<ExecutionHistory> getAllHistory() {
        return executionService.getAllHistory();
    }

    // Fetch history by ID
    @GetMapping("/history/{id}")
    public ExecutionHistory getHistoryById(@PathVariable String id) {
        return executionService.getHistoryById(id);
    }

    // Delete history by ID
    @DeleteMapping("/history/{id}")
    public String deleteHistory(@PathVariable String id) {
        executionService.deleteHistory(id);
        return "Execution history deleted successfully!";
    }
}
