package com.polyglotmesh;

import com.polyglotmesh.resource.ResourceLimitValidator;
import com.polyglotmesh.resource.ResourceLimits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceLimitTest {

    @Test
    void validatorRejectsUnsafeTimeout() {
        ResourceLimits limits = new ResourceLimits(
                301_000, 128 * 1024 * 1024, 1024 * 1024, 4, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new ResourceLimitValidator().validate(limits));
    }

    @Test
    void validatorRejectsOversizedOutputLimit() {
        ResourceLimits limits = new ResourceLimits(
                1000, 128 * 1024 * 1024, 11 * 1024 * 1024, 4, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new ResourceLimitValidator().validate(limits));
    }
}
