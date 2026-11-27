package com.polyglotmesh.execution.controller;

import com.polyglotmesh.execution.engine.ExecutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BackendStatusController {

    private final ExecutionService executionService;

    public BackendStatusController(ExecutionService executionService) {
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
}
