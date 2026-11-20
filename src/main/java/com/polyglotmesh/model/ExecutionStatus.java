package com.polyglotmesh.model;

public enum ExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMEOUT,
    RESOURCE_EXHAUSTED,
    SECURITY_VIOLATION,
    CANCELLED
}
