package com.polyglotmesh.execution.controller;

import com.polyglotmesh.entity.ExecutionHistory;
import com.polyglotmesh.service.ExecutionHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class ExecutionHistoryController {

    private final ExecutionHistoryService historyService;

    public ExecutionHistoryController(ExecutionHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<ExecutionHistory> getAllHistory() {
        return historyService.findAll();
    }

    @GetMapping("/{id}")
    public ExecutionHistory getHistoryById(@PathVariable String id) {
        return historyService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable String id) {
        historyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
