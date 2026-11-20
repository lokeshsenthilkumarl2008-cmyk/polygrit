package com.polyglotmesh.security;

public record SecurityViolation(SecurityViolationType type, String message) {
}
