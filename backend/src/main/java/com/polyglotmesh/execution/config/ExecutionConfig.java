package com.polyglotmesh.execution.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the execution engine.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "execution")
public class ExecutionConfig {

    /**
     * Default execution timeout in milliseconds.
     */
    private long defaultTimeoutMs = 30000; // 30 seconds

    /**
     * Maximum execution timeout allowed in milliseconds.
     */
    private long maxTimeoutMs = 120000; // 2 minutes

    /**
     * Default maximum output size in bytes.
     */
    private long defaultMaxOutputSize = 1024 * 1024; // 1 MB

    /**
     * Maximum allowed output size in bytes.
     */
    private long maxOutputSize = 10 * 1024 * 1024; // 10 MB

    /**
     * Whether to enable security restrictions for GraalVM contexts.
     */
    private boolean enableSecurityRestrictions = true;

    /**
     * Java compilation timeout in milliseconds.
     */
    private long javaCompilationTimeoutMs = 60000; // 60 seconds

    /**
     * Java execution timeout in milliseconds.
     */
    private long javaExecutionTimeoutMs = 30000; // 30 seconds

    /**
     * Temporary directory for Java compilation.
     * If not set, system default temp directory will be used.
     */
    private String javaTempDir;

    /**
     * Whether to allow host access in GraalVM contexts.
     */
    private boolean allowHostAccess = false;

    /**
     * Whether to allow host class lookup in GraalVM contexts.
     */
    private boolean allowHostClassLookup = false;

    /**
     * Whether to allow native access in GraalVM contexts.
     */
    private boolean allowNativeAccess = false;

    /**
     * Whether to allow IO operations in GraalVM contexts.
     */
    private boolean allowIO = false;

    /**
     * Whether to allow experimental options in GraalVM contexts.
     */
    private boolean allowExperimentalOptions = false;
}