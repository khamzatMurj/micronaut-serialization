package io.micronaut.serde.xml

import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import io.micronaut.serde.ObjectMapper
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.xml.XmlMapper

import java.nio.charset.StandardCharsets

trait XmlSpec {

    // JsonNode converter
    private static final tools.jackson.databind.ObjectMapper JACKSON_JSON = new tools.jackson.databind.ObjectMapper()
    private static final XmlMapper JACKSON_XML = XmlMapper.builder().build()

    abstract ObjectMapper getXmlMapper()

    JsonMapper getJsonMapper() { getXmlMapper() }

    String writeXml(Object bean) {
        new String(getXmlMapper().writeValueAsBytes(bean), StandardCharsets.UTF_8)
    }

    String writeXml(Argument argument, Object bean) {
        new String(getXmlMapper().writeValueAsBytes(argument, bean), StandardCharsets.UTF_8)
    }

    /**
     * Converting JSON string to XML with Jackson's JsonNode.
     */
    String xmlString(String json) {
        JsonNode tree = JACKSON_JSON.readTree(json)
        return JACKSON_XML.writeValueAsString(tree)
    }

    byte[] xmlBytes(String json) {
        return xmlString(json).getBytes(StandardCharsets.UTF_8)
    }

    // adding to json the missing root class name
    String expectedXml(String rootName, String json) {
        return xmlString("{\"${rootName}\":${json}}")
    }

    def <T> T serializeDeserialize(T obj) {
        def output = getXmlMapper().writeValueAsBytes(obj)
        return getXmlMapper().readValue(output, Argument.of(obj.getClass())) as T
    }

    def <T> T serializeDeserializeAs(T obj, Argument type) {
        def output = getXmlMapper().writeValueAsBytes(obj)
        return getXmlMapper().readValue(output, type) as T
    }
}
