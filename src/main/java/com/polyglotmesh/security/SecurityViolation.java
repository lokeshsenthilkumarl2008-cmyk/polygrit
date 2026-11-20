
package com.polyglotmesh.security;
public class SecurityViolation extends RuntimeException {

    private final String violationType;
    private final String executionId;
    public SecurityViolation(String message) {
        super(message);
        this.violationType = "GENERAL_SECURITY_VIOLATION";
        this.executionId = null;
    }
    public SecurityViolation(String violationType, String message) {
        super(message);
        this.violationType = violationType;
        this.executionId = null;
    }
    public SecurityViolation(
            String executionId,
            String violationType,
            String message) {

        super(message);
        this.executionId = executionId;
        this.violationType = violationType;
    }

    public String getViolationType() {
        return violationType;
    }

    public String getExecutionId() {
        return executionId;
    }
}
