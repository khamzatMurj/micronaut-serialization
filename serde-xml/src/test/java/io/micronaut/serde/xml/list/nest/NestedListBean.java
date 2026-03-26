package io.micronaut.serde.xml.list.nest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.serde.annotation.Serdeable;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;
import java.util.Objects;

/**
 * Class version of the former Java record:
 * {@code record NestedCollectionBean(List<StringGroup> strings, String prenom) {}}.
 */
@Serdeable
public class NestedListBean {

    @JacksonXmlElementWrapper(localName = "stringsA")
    private List<StringGroup> strings;
    private String prenom;

    public NestedListBean() {
        // default constructor
    }

    public NestedListBean(List<StringGroup> strings, String prenom) {
        this.strings = strings;
        this.prenom = prenom;
    }

    public List<StringGroup> getStrings() {
        return strings;
    }

    public void setStrings(List<StringGroup> strings) {
        this.strings = strings;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NestedListBean that = (NestedListBean) o;
        return Objects.equals(strings, that.strings) && Objects.equals(prenom, that.prenom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strings, prenom);
    }

    @Override
    public String toString() {
        return "NestedCollectionBean{" +
            "strings=" + strings +
            ", prenom='" + prenom + '\'' +
            '}';
    }
}
