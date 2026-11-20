package com.polyglotmesh.security;

public record SandboxConfiguration(
        boolean allowHostAccess,
        boolean allowHostClassLookup,
        boolean allowIo,
        boolean allowNativeAccess,
        boolean allowCreateProcess,
        boolean allowCreateThread,
        boolean allowEnvironmentAccess,
        boolean allowSystemProperties) {

    public static SandboxConfiguration untrusted() {
        return new SandboxConfiguration(false, false, false, false, false, false, false, false);
    }
}
