package io.micronaut.serde.xml;

import io.micronaut.serde.annotation.Serdeable;
import org.jspecify.annotations.Nullable;

@Serdeable
public class ArbitraryBean {

    @Nullable
    private String name;

    @Nullable
    private Object value;

//    @Nullable
//    private Object nested;
//
//    @Nullable
//    private List<Object> items;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }
}
