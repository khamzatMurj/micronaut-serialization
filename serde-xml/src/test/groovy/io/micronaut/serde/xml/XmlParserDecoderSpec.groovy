package io.micronaut.serde.xml

import io.micronaut.serde.LimitingStream
import org.intellij.lang.annotations.Language
import spock.lang.Specification
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.deser.FromXmlParser

import java.nio.charset.StandardCharsets

class XmlParserDecoderSpec extends Specification {

    def 'simpleElement'() {
        given:
        def decoder = decoderFor('<person><name>John</name></person>')

        when:
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'START_OBJECT'

        when:
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'KEY'
        decoder.getCurrentKey() == 'name'

        when:
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'STRING'
        decoder.getString() == 'John'
    }

    def 'attributes'() {
        given:
        def decoder = decoderFor('<person age="30"/>')

        when:
        decoder.nextToken()
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'KEY'
        decoder.getCurrentKey() == 'age'

        when:
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'STRING'
        decoder.getString() == '30'
    }

    def 'nestedObject'() {
        given:
        def decoder = decoderFor('<person><address><city>Paris</city></address></person>')

        expect:
        collectTokens(decoder) == [
            'START_OBJECT',
            'KEY',
            'START_OBJECT',
            'KEY',
            'STRING',
            'END_OBJECT',
            'END_OBJECT'
        ]
    }

    def 'array-like repeated elements'() {
        given:
        def decoder = decoderFor('<people><person>John</person><person>Jane</person></people>')

        expect:
        collectTokens(decoder) == [
            'START_OBJECT',
            'KEY',
            'STRING',
            'KEY',
            'STRING',
            'END_OBJECT'
        ]
    }

    def 'nested one-to-many objects'() {
        given:
        def decoder = decoderFor('<company><departments><department><name>Engineering</name></department><department><name>HR</name></department></departments></company>')
        decoder.nextToken()

        when:
        def root = decoder.decodeObject()

        then:
        root.decodeKey() == 'departments'

        when:
        def departments = root.decodeObject()

        then:
        departments.decodeKey() == 'department'

        when:
        def firstDepartment = departments.decodeObject()

        then:
        firstDepartment.decodeKey() == 'name'
        firstDepartment.decodeString() == 'Engineering'
        firstDepartment.decodeKey() == null
        firstDepartment.finishStructure()

        when:
        def secondDepartmentKey = departments.decodeKey()

        then:
        secondDepartmentKey == 'department'

        when:
        def secondDepartment = departments.decodeObject()

        then:
        secondDepartment.decodeKey() == 'name'
        secondDepartment.decodeString() == 'HR'
        secondDepartment.decodeKey() == null
        secondDepartment.finishStructure()
        departments.decodeKey() == null

        when:
        departments.finishStructure()

        then:
        root.decodeKey() == null

        when:
        root.finishStructure()

        then:
        noExceptionThrown()
    }

    def 'numbersAndBooleans'() {
        given:
        def decoder = decoderFor('<data><int>42</int><bool>true</bool><big>12345678901234567890</big></data>')
        decoder.nextToken()

        when:
        def object = decoder.decodeObject()

        then:
        object.decodeKey() == 'int'
        object.decodeLong() == 42L
        object.decodeKey() == 'bool'
        object.decodeBoolean()
        object.decodeKey() == 'big'
        object.decodeBigInteger() == new BigInteger('12345678901234567890')
        object.decodeKey() == null
    }

    def 'skipChildren'() {
        given:
        def decoder = decoderFor('<person><address><city>Paris</city></address><name>John</name></person>')

        when:
        decoder.nextToken()
        decoder.nextToken()
        decoder.nextToken()
        decoder.skipChildren()
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'KEY'
        decoder.getCurrentKey() == 'name'
    }

    def 'coerceScalarToString'() {
        given:
        def decoder = decoderFor('<data><number>42</number></data>')

        when:
        decoder.nextToken()
        decoder.nextToken()
        decoder.nextToken()

        then:
        decoder.currentToken().name() == 'STRING'
        decoder.coerceScalarToString(decoder.currentToken()) == '42'
    }

    private static XmlParserDecoder decoderFor(@Language('xml') String xml) {
        def parser = (FromXmlParser) new XmlMapper().createParser(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
        new XmlParserDecoder(parser, LimitingStream.DEFAULT_LIMITS)
    }

    private static List<String> collectTokens(XmlParserDecoder decoder) {
        def result = [] as List<String>
        while (true) {
            decoder.nextToken()
            def token = decoder.currentToken()
            if (token == null) {
                break
            }
            result.add(token.name())
        }
        result
    }
}
