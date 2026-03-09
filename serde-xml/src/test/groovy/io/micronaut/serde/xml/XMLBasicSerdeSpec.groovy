package io.micronaut.serde.xml

import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import io.micronaut.serde.AllTypesBean
import io.micronaut.serde.ApiResponse
import io.micronaut.serde.BeanWithExtraMethod
import io.micronaut.serde.ConstructorArgs
import io.micronaut.serde.Dummy
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.ObjectWithArray
import io.micronaut.serde.ObjectWithArrayConstructor
import io.micronaut.serde.ObjectWithArrayOfArray
import io.micronaut.serde.ObjectWithArrayRecord
import io.micronaut.serde.ObjectWithArrayRequired
import io.micronaut.serde.RecordBean
import io.micronaut.serde.Simple
import io.micronaut.serde.config.annotation.SerdeConfig
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import spock.lang.Ignore
import spock.lang.Specification
import tools.jackson.databind.JsonNode
import tools.jackson.dataformat.xml.XmlMapper

import java.nio.charset.StandardCharsets

@MicronautTest
class XmlBasicSerdeSpec extends Specification implements TestPropertyProvider, XmlSpec {


    @Inject
    @Named("xml")
    ObjectMapper xmlMapper

    @Override
    Map<String, String> getProperties() {
        ["micronaut.serde.serialization.inclusion": SerdeConfig.SerInclude.ALWAYS.name()]
    }



    // ---- Missing list tests ------------------------------------------------

    def "missing list"() {
        given:
        def xml = xmlBytes("{}")
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArray))
        then:
        obj
        obj.vals == null
    }

    def "missing list - constructor"() {
        given:
        def xml = xmlBytes("{}")
        println "==>" + xml
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArrayConstructor))
        then:
        obj
        println "==>" + obj
        obj.vals == null
    }

    def "missing list - record"() {
        given:
        def xml = xmlBytes("{}")
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArrayRecord))
        then:
        obj
        obj.vals() == null
    }

    def "missing list - required"() {
        given:
        def xml = xmlBytes("{}")
        when:
        xmlMapper.readValue(xml, Argument.of(ObjectWithArrayRequired))
        then:
        def e = thrown(Exception)
        e.message.contains("Required constructor parameter") || e.message.contains("Missing required creator property")
    }

    // ---- Simple bean -------------------------------------------------------

    void "test write simple"() {
        when:
        def bean = new Simple(name: "Test")
        def result = writeXml(bean)
        then:
        //result == expectedXml("Simple", '{"name":"Test"}')
        result == '<Simple><name>Test</name></Simple>'
    }

    // ---- Constructor args --------------------------------------------------

    void "test read/write constructor args"() {
        when:
        def bean = new ConstructorArgs("test", 100)
        bean.author = "Bob"
        bean.other = "Something"
        def result = writeXml(bean)
        then:
        result.contains("<title>test</title>")
        result.contains("<author>Bob</author>")
        result.contains("<pages>100</pages>")
        result.contains("<other>Something</other>")
        result.startsWith("<ConstructorArgs>")
        result.trim().endsWith("</ConstructorArgs>")

        when:
        bean = xmlMapper.readValue(result, Argument.of(ConstructorArgs))
        then:
        bean.title == 'test'
        bean.pages == 100
        bean.other == 'Something'
        bean.author == 'Bob'

        when:
        bean = xmlMapper.readValue(
                xmlBytes('{"other":"Something","author":"Bob","title":"test","pages":100}'),
                Argument.of(ConstructorArgs))
        then:
        bean.title == 'test'
        bean.pages == 100
        bean.other == 'Something'
        bean.author == 'Bob'
    }

    // ---- Record ------------------------------------------------------------

    void "test read/write record"() {
        when:
        def bean = new RecordBean("fizz", "buzz")
        def result = writeXml(bean)
        then:
        //result == expectedXml("RecordBean", '{"foo":"fizz","bar":"buzz"}')
        result == '<RecordBean><foo>fizz</foo><bar>buzz</bar></RecordBean>'

        when:
        bean = xmlMapper.readValue(result, Argument.of(RecordBean))
        then:
        bean.foo() == 'fizz'
        bean.bar() == 'buzz'

        when:
        bean = xmlMapper.readValue(
                xmlBytes('{"foo":"fizz","ignore":"this","bar":"buzz"}'),
                Argument.of(RecordBean))
        then:
        bean.foo() == 'fizz'
        bean.bar() == 'buzz'
    }

    // ---- Extra executable method -------------------------------------------

    void "test a bean with an extra executable method"() {
        when:
        def bean = new BeanWithExtraMethod()
        bean.name = "Bob"
        def result = writeXml(bean)
        then:
        //result == expectedXml("BeanWithExtraMethod", '{"name":"Bob"}')
        result == '<BeanWithExtraMethod><name>Bob</name></BeanWithExtraMethod>'

        when:
        bean = xmlMapper.readValue(result, Argument.of(BeanWithExtraMethod))
        then:
        bean.name == 'Bob'
    }

    // ---- All-types round-trip ----------------------------------------------

    def "should deser all null types bean"() {
        when:
        def obj = xmlMapper.readValue(xmlBytes("{}"), Argument.of(AllTypesBean))
        then:
        noExceptionThrown()
    }

    def "validate all types bean"() {
        given:
        def all = new AllTypesBean()
        all.someBool = true
        all.someInt = 123
        all.someLong = 234
        all.someByte = (byte) 34
        all.someShort = (short) 567
        all.someFloat = 11.22f
        all.someDouble = 123.234D
        all.someString = "Hello"
        all.someBoolean = Boolean.TRUE
        all.someInteger = 444
        all.someLongObj = 555
        all.someDoubleObj = 666.77d
        all.someShortObj = 777
        all.someFloatObj = 888.99f
        all.someByteObj = 99
        all.bigDecimal = BigDecimal.valueOf(12345.12345)
        all.bigInteger = BigInteger.valueOf(123456789)
        when:
        def result = serializeDeserialize(all)
        then:
        result.someBool
        result.someInt == 123
        result.someLong == 234
        result.someByte == (byte) 34
        result.someShort == (short) 567
        result.someFloat == 11.22f
        result.someDouble == 123.234D
        result.someString == "Hello"
        result.someBoolean == Boolean.TRUE
        result.someInteger == 444
        result.someLongObj == 555
        result.someDoubleObj == 666.77d
        result.someShortObj == 777
        result.someFloatObj == 888.99f
        result.someByteObj == 99
        result.bigDecimal == BigDecimal.valueOf(12345.12345)
        result.bigInteger == BigInteger.valueOf(123456789)
    }

    def "all nullable fields null"() {
        given:
        def bean = new AllTypesBean()
        // leave all nullable fields unset (null)

        when:
        def bytes = xmlMapper.writeValueAsBytes(bean)
        def result = xmlMapper.readValue(bytes, Argument.of(AllTypesBean))

        then:
        noExceptionThrown()
        !result.someBool
        result.someInt   == 0
        result.someLong  == 0L
        result.someString  == null
        result.someBoolean == null
        result.bigDecimal  == null
        result.bigInteger  == null
    }

    // ---- Nested ------------------------------------------------------------

    @Ignore("XML lists without wrappers deserialize single items as Objects/Maps initially")
    void "test nested"() {
        when:
        def bean = new ApiResponse(List.of(new Dummy("Xyz"), new Dummy("xcv")))
        def argument = Argument.of(ApiResponse, Argument.listOf(Dummy))
        def result = writeXml(argument, bean)

        then:
        //result == expectedXml("ApiResponse", '{"content":[{"name":"Xyz"}, {"name":"xcv"}]}')
        result == '<ApiResponse><content><name>Xyz</name></content><content><name>xcv</name></content></ApiResponse>'

        when:
        def readBean = xmlMapper.readValue(result.getBytes(StandardCharsets.UTF_8), argument)
        println readBean.toString()
//        readBean. == "Xyz"
        println readBean.content().get('name') == "Xyz"

        then:
            true
    }

    // ---- Validate Arrays as Null -------------------------------------------

    def "validate arrays as null"() {
        given:
        def xml = xmlBytes('{"ObjectWithArray":{"vals": null}}')
        println xmlString('{"ObjectWithArray":{"vals": null}}')
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArray))
        then:
        obj
        obj.vals == null
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements / SerdeException in arrays")
    def "validate arrays"() {
        given:
        def xml = '<ObjectWithArray><vals><val>A</val></vals><vals><val>B</val></vals></ObjectWithArray>'
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArray))
        then:
        obj
        obj.vals.size() == 2
        obj.vals[0].val == "A"
        obj.vals[1].val == "B"
        writeXml(obj) == xml
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements / NullPointerException in empty arrays")
    def "validate empty arrays"() {
        given:
        // def xml = expectedXml("ObjectWithArray", '{"vals": []}')
        def xml = '<ObjectWithArray/>'
        when:
        // def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(ObjectWithArray))
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArray))
        then:
        obj
        obj.vals.size() == 0
        writeXml(obj) == xml
    }

    @Ignore("Pending array of nulls support in XML")
    def "validate arrays with nulls"() {
        given:
        def xml = '<ObjectWithArray><vals><val>A</val></vals><vals/><vals><val>B</val></vals></ObjectWithArray>'
        when:
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArray))
        then:
        obj
        obj.vals.size() == 3
        obj.vals[0].val == "A"
        obj.vals[1] == null
        obj.vals[2].val == "B"
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements")
    def "validate arrays of arrays"() {
        given:
        def xml = '<ObjectWithArrayOfArray><vals><vals><val>A</val></vals><vals/><vals><val>B</val></vals></vals></ObjectWithArrayOfArray>'
        when:
        def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(ObjectWithArrayOfArray))
        then:
        obj
        obj.vals.size() == 1
        obj.vals[0].size() == 3
        obj.vals[0][0].val == "A"
        obj.vals[0][1] == null
        obj.vals[0][2].val == "B"
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements")
    def "validate empty arrays of arrays"() {
        given:
        // def xml = expectedXml("ObjectWithArrayOfArray", '{"vals": [[]]}')
        def xml = '<ObjectWithArrayOfArray><vals/></ObjectWithArrayOfArray>'
        when:
        // def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(ObjectWithArrayOfArray))
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArrayOfArray))
        then:
        obj
        obj.vals.size() == 1
        obj.vals[0].size() == 0
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements")
    def "validate null arrays of arrays"() {
        given:
        // def xml = expectedXml("ObjectWithArrayOfArray", '{"vals": [null]}')
        def xml = '<ObjectWithArrayOfArray><vals/></ObjectWithArrayOfArray>'
        when:
        // def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(ObjectWithArrayOfArray))
        def obj = xmlMapper.readValue(xml, Argument.of(ObjectWithArrayOfArray))
        then:
        obj
        obj.vals.size() == 1
        obj.vals[0] == null
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements / SerdeException in arrays")
    def "should deser deep structure Users 1"() {
        given:
            def json = """{"users":[{"_id":"39771757156730064829","index":1031703887,"guid":"ifhsrU6geU4PijjDE8Q5","isActive":false,"balance":"TKl0GcwTs72S4CPx5rfg","picture":"FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8","age":5,"eyeColor":"AY79Pw4sYByUZEMLxnYJ","name":"XjXrEZMuTvPnuOPBg7hL","gender":"VaMcuWBHvnWvIlCC9q4T","company":"6pmCe1LxouRGfZD79ena","email":"TboNtpmAS0ppZ07jITFE","phone":"j8OoUhtmwBlI20EgD1LS","address":"Aqo4fSYBpvvAWTDqbFbK","about":"1kXFSA2782BLqNBbKIbp","registered":"Mc7h3gZJcQ11ShGQYdXI","latitude":13.474549605725421,"longitude":35.010833129741435,"tags":["8tGfPhZkZD","XYmwuAAtZ4","u9iBDMpS9G","4udy1eRqme","Lg48Ogrf0I","zku019kVpo","iuIMkiZzog","MuI1uYeCjc","49n7qisFD8","TtVgWerCRh","H604QRJmi1","ZIQMfqInNH","CbDyjjA19F","pNFwPdkVdU","aPFLsUbIUh","fA735PT0Hd","00etYDYL87","mlyEf1lI2B","RQ05IJSzXF","3jJt0Zrkhw","ZINP8GH4Bm","XebX8UvviN","EXqZ9G0ATB","ssyzWZVAa2"],"friends":[{"id":"2668","name":"lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe"},{"id":"9395","name":"dxNBbezfkbotyCmFzjodONShlGFaAg"},{"id":"5249","name":"fYHSDXScMSzQvxzFuuPHYWfyjdGQLg"},{"id":"4978","name":"qfoxPWmoWUyUduVkRwhzyBusuflrFY"},{"id":"9710","name":"vUAJwshFGLoBHfwLcsEVNLJLwdaCAg"},{"id":"7404","name":"BhVMdvhPRdpwpDWAmfhNDikncdNgGr"},{"id":"1343","name":"ZeDoizPcOBafZtVYDOmpzGoHekfoxf"},{"id":"7382","name":"KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG"},{"id":"1365","name":"rCSTlgbmTAFhbSfPmnftcDLwdiKsHt"},{"id":"8037","name":"PUvwVYoSvSTnwjJCQITTcwNvMOpxie"},{"id":"4858","name":"cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG"},{"id":"9141","name":"rJxMGOWRjdkphthcaKTspFrMcvcLLb"},{"id":"9128","name":"gcsYaolAQqrNMQTluIAKOkwYTWVUXe"},{"id":"2268","name":"jwXOUcXAiLurRlgTdxyKWvsbNHfFxl"},{"id":"5447","name":"whivfJXOdxoHtLIGpytTdbOXxlZpUY"},{"id":"7551","name":"whykuIjZUgvOFGpmNHjoPeTeYCPNby"},{"id":"719","name":"SmbiwQaORLdsbAlUZbQwgCKfuoPLVr"},{"id":"7773","name":"LZmRMXmXXHzlzFFJAopDNnWkuBqndD"},{"id":"9602","name":"xCNsDBFMygEwZuecJKTUrqeDLBJlrR"},{"id":"1536","name":"hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB"},{"id":"3549","name":"NvvhXwWgCSaYijqhxsrxIWrHbBOOIa"}],"greeting":"hTAIJLspvLr8DJPG3jYh","favoriteFruit":"f6ZsZ3saRGKMBCZLAkiP"}]}"""
            def xml = expectedXml("Users1", json)
        when:
            def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(Users1))
        then:
            obj
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements / SerdeException in arrays")
    def "should deser deep structure Users 2"() {
        given:
            def json = """{"users":[{"_id":"39771757156730064829","index":1031703887,"guid":"ifhsrU6geU4PijjDE8Q5","isActive":false,"balance":"TKl0GcwTs72S4CPx5rfg","picture":"FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8","age":5,"eyeColor":"AY79Pw4sYByUZEMLxnYJ","name":"XjXrEZMuTvPnuOPBg7hL","gender":"VaMcuWBHvnWvIlCC9q4T","company":"6pmCe1LxouRGfZD79ena","email":"TboNtpmAS0ppZ07jITFE","phone":"j8OoUhtmwBlI20EgD1LS","address":"Aqo4fSYBpvvAWTDqbFbK","about":"1kXFSA2782BLqNBbKIbp","registered":"Mc7h3gZJcQ11ShGQYdXI","latitude":13.474549605725421,"longitude":35.010833129741435,"tags":["8tGfPhZkZD","XYmwuAAtZ4","u9iBDMpS9G","4udy1eRqme","Lg48Ogrf0I","zku019kVpo","iuIMkiZzog","MuI1uYeCjc","49n7qisFD8","TtVgWerCRh","H604QRJmi1","ZIQMfqInNH","CbDyjjA19F","pNFwPdkVdU","aPFLsUbIUh","fA735PT0Hd","00etYDYL87","mlyEf1lI2B","RQ05IJSzXF","3jJt0Zrkhw","ZINP8GH4Bm","XebX8UvviN","EXqZ9G0ATB","ssyzWZVAa2"],"friends":[{"id":"2668","name":"lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe"},{"id":"9395","name":"dxNBbezfkbotyCmFzjodONShlGFaAg"},{"id":"5249","name":"fYHSDXScMSzQvxzFuuPHYWfyjdGQLg"},{"id":"4978","name":"qfoxPWmoWUyUduVkRwhzyBusuflrFY"},{"id":"9710","name":"vUAJwshFGLoBHfwLcsEVNLJLwdaCAg"},{"id":"7404","name":"BhVMdvhPRdpwpDWAmfhNDikncdNgGr"},{"id":"1343","name":"ZeDoizPcOBafZtVYDOmpzGoHekfoxf"},{"id":"7382","name":"KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG"},{"id":"1365","name":"rCSTlgbmTAFhbSfPmnftcDLwdiKsHt"},{"id":"8037","name":"PUvwVYoSvSTnwjJCQITTcwNvMOpxie"},{"id":"4858","name":"cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG"},{"id":"9141","name":"rJxMGOWRjdkphthcaKTspFrMcvcLLb"},{"id":"9128","name":"gcsYaolAQqrNMQTluIAKOkwYTWVUXe"},{"id":"2268","name":"jwXOUcXAiLurRlgTdxyKWvsbNHfFxl"},{"id":"5447","name":"whivfJXOdxoHtLIGpytTdbOXxlZpUY"},{"id":"7551","name":"whykuIjZUgvOFGpmNHjoPeTeYCPNby"},{"id":"719","name":"SmbiwQaORLdsbAlUZbQwgCKfuoPLVr"},{"id":"7773","name":"LZmRMXmXXHzlzFFJAopDNnWkuBqndD"},{"id":"9602","name":"xCNsDBFMygEwZuecJKTUrqeDLBJlrR"},{"id":"1536","name":"hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB"},{"id":"3549","name":"NvvhXwWgCSaYijqhxsrxIWrHbBOOIa"}],"greeting":"hTAIJLspvLr8DJPG3jYh","favoriteFruit":"f6ZsZ3saRGKMBCZLAkiP"}]}"""
            def xml = expectedXml("Users2", json)
        when:
            def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(Users2))
        then:
            obj
    }

    @Ignore("Jackson XML struggles with nested lists without wrapper elements / SerdeException in arrays")
    def "should deser deep structure Users 3"() {
        given:
            def json = """{"users":[{"_id":"39771757156730064829","index":1031703887,"guid":"ifhsrU6geU4PijjDE8Q5","isActive":false,"balance":"TKl0GcwTs72S4CPx5rfg","picture":"FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8","age":5,"eyeColor":"AY79Pw4sYByUZEMLxnYJ","name":"XjXrEZMuTvPnuOPBg7hL","gender":"VaMcuWBHvnWvIlCC9q4T","company":"6pmCe1LxouRGfZD79ena","email":"TboNtpmAS0ppZ07jITFE","phone":"j8OoUhtmwBlI20EgD1LS","address":"Aqo4fSYBpvvAWTDqbFbK","about":"1kXFSA2782BLqNBbKIbp","registered":"Mc7h3gZJcQ11ShGQYdXI","latitude":13.474549605725421,"longitude":35.010833129741435,"tags":["8tGfPhZkZD","XYmwuAAtZ4","u9iBDMpS9G","4udy1eRqme","Lg48Ogrf0I","zku019kVpo","iuIMkiZzog","MuI1uYeCjc","49n7qisFD8","TtVgWerCRh","H604QRJmi1","ZIQMfqInNH","CbDyjjA19F","pNFwPdkVdU","aPFLsUbIUh","fA735PT0Hd","00etYDYL87","mlyEf1lI2B","RQ05IJSzXF","3jJt0Zrkhw","ZINP8GH4Bm","XebX8UvviN","EXqZ9G0ATB","ssyzWZVAa2"],"friends":[{"id":"2668","name":"lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe"},{"id":"9395","name":"dxNBbezfkbotyCmFzjodONShlGFaAg"},{"id":"5249","name":"fYHSDXScMSzQvxzFuuPHYWfyjdGQLg"},{"id":"4978","name":"qfoxPWmoWUyUduVkRwhzyBusuflrFY"},{"id":"9710","name":"vUAJwshFGLoBHfwLcsEVNLJLwdaCAg"},{"id":"7404","name":"BhVMdvhPRdpwpDWAmfhNDikncdNgGr"},{"id":"1343","name":"ZeDoizPcOBafZtVYDOmpzGoHekfoxf"},{"id":"7382","name":"KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG"},{"id":"1365","name":"rCSTlgbmTAFhbSfPmnftcDLwdiKsHt"},{"id":"8037","name":"PUvwVYoSvSTnwjJCQITTcwNvMOpxie"},{"id":"4858","name":"cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG"},{"id":"9141","name":"rJxMGOWRjdkphthcaKTspFrMcvcLLb"},{"id":"9128","name":"gcsYaolAQqrNMQTluIAKOkwYTWVUXe"},{"id":"2268","name":"jwXOUcXAiLurRlgTdxyKWvsbNHfFxl"},{"id":"5447","name":"whivfJXOdxoHtLIGpytTdbOXxlZpUY"},{"id":"7551","name":"whykuIjZUgvOFGpmNHjoPeTeYCPNby"},{"id":"719","name":"SmbiwQaORLdsbAlUZbQwgCKfuoPLVr"},{"id":"7773","name":"LZmRMXmXXHzlzFFJAopDNnWkuBqndD"},{"id":"9602","name":"xCNsDBFMygEwZuecJKTUrqeDLBJlrR"},{"id":"1536","name":"hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB"},{"id":"3549","name":"NvvhXwWgCSaYijqhxsrxIWrHbBOOIa"}],"greeting":"hTAIJLspvLr8DJPG3jYh","favoriteFruit":"f6ZsZ3saRGKMBCZLAkiP"}]}"""
            def xml = expectedXml("Users3", json)
        when:
            def obj = xmlMapper.readValue(xmlBytes(xml), Argument.of(Users3))
        then:
            obj
    }


    def "validate json node"() {
        when:
        def result = serializeDeserializeAs(
            io.micronaut.json.tree.JsonNode.createObjectNode(["v": jsonNode]), Argument.of(io.micronaut.json.tree.JsonNode.class)).get("v")

        then:
        if (jsonNode.isNumber() && result.isNumber()) {
            Math.abs(result.doubleValue - jsonNode.doubleValue) < 0.000001
        } else {
            result.value == jsonNode.value || result.value.toString() == jsonNode.value.toString()
        }

        where:
        jsonNode << [
            io.micronaut.json.tree.JsonNode.createBooleanNode(true),
            io.micronaut.json.tree.JsonNode.createNumberNode(123),
            io.micronaut.json.tree.JsonNode.createNumberNode(234L),
            io.micronaut.json.tree.JsonNode.createNumberNode(11.22f),
            io.micronaut.json.tree.JsonNode.createNumberNode(123.234D),
            io.micronaut.json.tree.JsonNode.createNumberNode(BigInteger.valueOf(123456789)),
            io.micronaut.json.tree.JsonNode.createNumberNode(BigDecimal.valueOf(12345.12345)),
            io.micronaut.json.tree.JsonNode.createStringNode("Hello"),
        ]
    }

    // ---- Skip unknown / decode null ----------------------------------------

    def "should skip unknown values"() {
        when:
        def value = xmlMapper.readValue(xmlBytes('{"unknown":"ABC"}'), Argument.of(AllTypesBean))

        then:
        noExceptionThrown()
    }

    def "should decode null via Jackson XmlMapper"() {
        when:
        def value = xmlMapper.readValue(
                xmlBytes('{"someBool":null,"someInt":null,"bigDecimal":null}'),
                Argument.of(AllTypesBean))
        then:
        value.someInt == 0
        !value.someBool
        value.bigDecimal == null
    }
    // Type-level round-trips (primitives as element text)

    def "round-trip primitive types via AllTypesBean"() {
        given:
        def bean = new AllTypesBean()
        bean.someInt    = intVal
        bean.someLong   = longVal
        bean.someDouble = doubleVal
        bean.someBool   = boolVal

        when:
        def bytes  = xmlMapper.writeValueAsBytes(bean)
        def result = xmlMapper.readValue(bytes, Argument.of(AllTypesBean))

        then:
        result.someInt    == intVal
        result.someLong   == longVal
        Math.abs(result.someDouble - doubleVal) < 0.001d
        result.someBool   == boolVal

        where:
        intVal | longVal    | doubleVal | boolVal
        0      | 0L         | 0.0d      | false
        1      | 1L         | 1.1d      | true
        -42    | -100000L   | -3.14d    | false
        999    | 9999999999L| 123.456d  | true
    }


}
