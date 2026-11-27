package com.polyglotmesh.service;

import com.polyglotmesh.entity.ExecutionHistory;
import com.polyglotmesh.execution.model.ExecutionRequest;
import com.polyglotmesh.execution.model.ExecutionResult;
import com.polyglotmesh.exception.ResourceNotFoundException;
import com.polyglotmesh.repository.ExecutionHistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionHistoryService {

    private final ExecutionHistoryRepository historyRepository;

    public ExecutionHistoryService(ExecutionHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void record(ExecutionRequest request, ExecutionResult result) {
        ExecutionHistory history = new ExecutionHistory();
        history.setLanguage(result.getLanguage());
        history.setCode(request.getCode());
        history.setOutput(result.getOutput());
        history.setStatus(result.getStatus().name());
        history.setError(result.getError());
        history.setExecutionTime(result.getExecutionTime());
        historyRepository.save(history);
    }

    public List<ExecutionHistory> findAll() {
        return historyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public ExecutionHistory findById(String id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execution history not found: " + id));
    }

    public void deleteById(String id) {
        if (!historyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Execution history not found: " + id);
        }
        historyRepository.deleteById(id);
    }
}
