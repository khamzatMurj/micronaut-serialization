package io.micronaut.serde.xml;

import io.micronaut.serde.annotation.Serdeable;
import org.jspecify.annotations.Nullable;

/**
 * Minimal bean with only an {@code Object} field,
 * used to isolate and verify nested-object-as-Map deserialization
 * via {@code decodeArbitrary()} without unrelated fields interfering.
 */
@Serdeable
public class NestedOnlyBean {

    @Nullable
    private Object nested;

    @Nullable
    public Object getNested() {
        return nested;
    }

    public void setNested(@Nullable Object nested) {
        this.nested = nested;
    }
}
