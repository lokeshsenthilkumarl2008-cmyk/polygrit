package com.polyglotmesh.security;

import java.util.Locale;

public final class AccessControlManager {

    public SecurityViolation classify(Throwable throwable) {
        String message = throwable == null ? "" : String.valueOf(throwable.getMessage());
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("host class") || lower.contains("java.") || lower.contains("host access")) {
            return new SecurityViolation(SecurityViolationType.HOST_CLASS_ACCESS, message);
        }
        if (lower.contains("file") || lower.contains("readfile") || lower.contains("io")) {
            return new SecurityViolation(SecurityViolationType.FILE_SYSTEM_ACCESS, message);
        }
        if (lower.contains("network") || lower.contains("socket") || lower.contains("http")) {
            return new SecurityViolation(SecurityViolationType.NETWORK_ACCESS, message);
        }
        if (lower.contains("environment")) {
            return new SecurityViolation(SecurityViolationType.ENVIRONMENT_ACCESS, message);
        }
        if (lower.contains("property")) {
            return new SecurityViolation(SecurityViolationType.SYSTEM_PROPERTY_ACCESS, message);
        }
        return new SecurityViolation(SecurityViolationType.UNAUTHORIZED_OPERATION, message);
    }

    public boolean isAccessViolation(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            String name = current.getClass().getName();
            if (name.contains("PolyglotException")
                    || name.contains("SecurityException")
                    || name.contains("AccessControlException")) {
                return true;
            }
        }
        return false;
    }
}
