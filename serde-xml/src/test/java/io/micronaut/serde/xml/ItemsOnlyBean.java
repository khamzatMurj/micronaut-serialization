package io.micronaut.serde.xml;

import io.micronaut.serde.annotation.Serdeable;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Serdeable
public class ItemsOnlyBean {

    @Nullable
    private List<Object> items;

    @Nullable
    public List<Object> getItems() {
        return items;
    }

    public void setItems(@Nullable List<Object> items) {
        this.items = items;
    }
}
