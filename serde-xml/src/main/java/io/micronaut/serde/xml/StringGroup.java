package io.micronaut.serde.xml;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Objects;

/**
 * Wraps a list of strings so we don't need to model this as {@code List<List<String>>}.
 */
@Serdeable
public class StringGroup {

    private List<String> list;

    public StringGroup() {
        // default constructor
    }

    public StringGroup(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringGroup that = (StringGroup) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "StringGroup{" +
            "list=" + list +
            '}';
    }
}
