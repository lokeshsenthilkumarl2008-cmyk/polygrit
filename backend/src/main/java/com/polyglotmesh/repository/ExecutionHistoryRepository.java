
package com.polyglotmesh.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.polyglotmesh.entity.ExecutionHistory;

public interface ExecutionHistoryRepository
        extends MongoRepository<ExecutionHistory, String> {

}
