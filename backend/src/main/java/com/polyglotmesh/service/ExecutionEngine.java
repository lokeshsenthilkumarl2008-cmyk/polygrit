
package com.polyglotmesh.service;

import com.polyglotmesh.dto.ExecutionResponse;

public interface ExecutionEngine {

    ExecutionResponse execute(String language, String code);

}
