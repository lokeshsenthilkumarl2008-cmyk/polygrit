
package com.polyglotmesh.service;

import org.springframework.stereotype.Component;

import com.polyglotmesh.dto.ExecutionResponse;

@Component
public class PendingExecutionEngine implements ExecutionEngine {

    @Override
    public ExecutionResponse execute(String language, String code) {

        return new ExecutionResponse(
                null,
                "PENDING",
                "Execution engine is not connected yet."
        );
    }
}
