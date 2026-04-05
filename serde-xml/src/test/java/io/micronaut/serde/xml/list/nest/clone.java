package io.micronaut.serde.xml.list.nest;

import io.micronaut.serde.xml.XmlObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.List;

@MicronautTest
public class clone {


    @Inject
    @Named("xml")
    XmlObjectMapper mapper;

    @Test
    void serialization() throws Exception {
        NestedListBean bean = new NestedListBean(
            List.of(
                new StringGroup(List.of("foo", "bar")),
                new StringGroup(List.of("baz"))
            ),
            "salamander"
        );
        System.out.println(mapper.writeValueAsString(bean));
        System.out.println(XmlMapper.builder().build().writeValueAsString(bean));

        assert 1==1;
    }
}
