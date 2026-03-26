package io.micronaut.serde.xml.map;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.xml.XmlObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class map {

    @Test
    void test() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("hello", "world");
        map.put("lists", Arrays.asList("1", "2", "3"));

        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            String xml = xmlMapper.writeValueAsString(map);

            System.out.println(xml);
        }
    }
}
