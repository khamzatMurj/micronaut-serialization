package io.micronaut.serde.xml.list.nest;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.xml.XmlObjectMapper;
import io.micronaut.serde.xml.list.nest.NestedListBean;
import io.micronaut.serde.xml.list.nest.StringGroup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class NestedCollectionBeanStringGroupTest {

    @Inject
    @Named("xml")
    XmlObjectMapper mapper;

    @Test
    void nestedCollectionBeanWithWrapperTypeRoundTrip() throws Exception {
        NestedListBean expected = new NestedListBean(
            List.of(
                new StringGroup(List.of("foo", "bar")),
                new StringGroup(List.of("baz"))
            ),
            "salamander"
        );


        // Same structure as other nested collection tests: outer element is property name (<strings>)
        // and each nested element is derived from the nested type name (<StringGroup> / <list>).
        String expectedXml = """
            <NestedCollectionBean>
            	<prenom>salamander</prenom>
            	<strings>
            		<strings>
            			<list>
            				<list>foo</list>
            				<list>bar</list>
            			</list>
            		</strings>
            		<strings>
            			<list>
            				<list>baz</list>
            			</list>
            		</strings>
            	</strings>
            </NestedCollectionBean>
            """;


        NestedListBean decoded = mapper.readValue(expectedXml, NestedListBean.class);
        assertEquals(expected, decoded);
    }

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
