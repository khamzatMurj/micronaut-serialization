package io.micronaut.serde.xml;

import io.micronaut.context.ApplicationContext;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.List;

public class testing {
    public static void main(String[] args) throws IOException {
        NestedListBean pojo = new NestedListBean(
            List.of(
                new StringGroup(List.of("foo", "bar")),
                new StringGroup(List.of("baz"))
            ),
            "salamander"
        );
        try (ApplicationContext ctx = ApplicationContext.run()) {
            var bean = ctx.getBean(XmlObjectMapper.class);
            var xml = bean.writeValueAsString(pojo);
            System.out.println(xml);
            System.out.println(XmlMapper.builder().build().writeValueAsString(pojo));


        }
    }
}
