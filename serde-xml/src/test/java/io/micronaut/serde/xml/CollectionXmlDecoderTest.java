package io.micronaut.serde.xml;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
//import io.micronaut.serde.data.Users3;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class CollectionXmlDecoderTest {

    @Inject
    @Named("xml")
    XmlObjectMapper mapper;

    private static <T> String writeXml(ObjectMapper xmlMapper, Argument<T> argument, T bean) throws IOException {
        return new String(xmlMapper.writeValueAsBytes(argument, bean), StandardCharsets.UTF_8);
    }

    @Test
    void testDecodeStringListBean() throws Exception {
        String xml = "<StringListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></StringListBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.StringListBean.class);

        CollectionXmlEncoderSerdeTest.StringListBean expected =
            new CollectionXmlEncoderSerdeTest.StringListBean(23, "ali", List.of("foo", "bar"), "salamander");

        System.out.println("Actual: " + actual);
        System.out.println("Expected: " + expected);
        assertEquals(expected, actual);

        String serialized = writeXml(mapper, Argument.of(CollectionXmlEncoderSerdeTest.StringListBean.class), expected);
        assertEquals(xml, serialized);



    }

    @Test
    void testDecodeAndEncodeMapLikeJacksonXmlMapper() throws Exception {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("name", "hamza");
        original.put("age", 25);
        original.put("active", true);
        original.put("skills", List.of("java", "jackson", "xml"));

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("city", "Sale");
        address.put("street", "06");
        original.put("address", address);

        Argument<Map<String, Object>> mapArgument = Argument.mapOf(String.class, Object.class);

        String serialized = writeXml(mapper, mapArgument, original);
        assertEquals(
            "<Map><name>hamza</name><age>25</age><active>true</active><skills><skills>java</skills><skills>jackson</skills><skills>xml</skills></skills><address><city>Sale</city><street>06</street></address></Map>",
            serialized
        );

        Map<String, Object> deserialized = mapper.readValue(serialized, mapArgument);
        deserialized.forEach((k, v) -> {
            System.out.println("key: " + k + " value: " + v);
        });
        assertNotNull(deserialized);
        assertEquals("hamza", deserialized.get("name"));
        assertEquals("25", deserialized.get("age"));
        assertEquals("true", deserialized.get("active"));
        assertEquals(List.of("java", "jackson", "xml"), deserialized.get("skills"));
        assertEquals(Map.of("city", "Sale", "street", "06"), deserialized.get("address"));
    }

    @Test
    void testDecodeArrayListBean() throws Exception {
        String xml = "<ArrayListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></ArrayListBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.ArrayListBean.class);

        CollectionXmlEncoderSerdeTest.ArrayListBean expected =
            new CollectionXmlEncoderSerdeTest.ArrayListBean(23, "ali",
                new ArrayList<>(List.of("foo", "bar")), "salamander");

        assertEquals(expected, actual);
    }

    @Test
    void testDecodeLinkedListBean() throws Exception {
        String xml = "<LinkedListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></LinkedListBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.LinkedListBean.class);

        CollectionXmlEncoderSerdeTest.LinkedListBean expected =
            new CollectionXmlEncoderSerdeTest.LinkedListBean(23, "ali",
                new LinkedList<>(List.of("foo", "bar")), "salamander");

        assertEquals(expected, actual);
    }

    @Test
    void testDecodeLinkedHashSetBean() throws Exception {
        String xml = "<LinkedHashSetBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></LinkedHashSetBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.LinkedHashSetBean.class);

        LinkedHashSet<String> set = new LinkedHashSet<>(List.of("foo", "bar"));
        CollectionXmlEncoderSerdeTest.LinkedHashSetBean expected =
            new CollectionXmlEncoderSerdeTest.LinkedHashSetBean(23, "ali", set, "salamander");

        assertEquals(expected, actual);
    }

    @Test
    void testDecodeCollectionBean() throws Exception {
        String xml = "<CollectionBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></CollectionBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.CollectionBean.class);

        Collection<String> col = new ArrayList<>(List.of("foo", "bar"));
        CollectionXmlEncoderSerdeTest.CollectionBean expected =
            new CollectionXmlEncoderSerdeTest.CollectionBean(23, "ali", col, "salamander");

        assertEquals(expected, actual);
    }

    @Test
    void testDecodeNestedCollectionBean() throws Exception {
        //String xml = "<NestedCollectionBean><strings><strings><strings>foo</strings><strings>bar</strings></strings><strings><strings>baz</strings></strings></strings><prenom>salamander</prenom></NestedCollectionBean>";
        String xml = "<NestedCollectionBean><strings><String><String>foo</String><String>bar</String></String><String><String>baz</String></String></strings><prenom>salamander</prenom></NestedCollectionBean>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.NestedCollectionBean.class);

        List<List<String>> nested =
            List.of(List.of("foo", "bar"), List.of("baz"));
        CollectionXmlEncoderSerdeTest.NestedCollectionBean expected =
            new CollectionXmlEncoderSerdeTest.NestedCollectionBean(nested, "salamander");

        assertEquals(expected, actual);
        String serialized = writeXml(mapper, Argument.of(CollectionXmlEncoderSerdeTest.NestedCollectionBean.class), expected);
        System.out.println(serialized);
        assertEquals(xml, serialized);

    }

    @Test
    void testDecodeNestedCubicTime() throws Exception {
        String xml = "<NestedCubicTime><strings><strings><strings><strings>fifi</strings><strings>abdo</strings></strings></strings></strings><prenom>fayrouz</prenom></NestedCubicTime>";
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, CollectionXmlEncoderSerdeTest.NestedCubicTime.class);

        List<List<List<String>>> cubic =
            List.of(List.of(List.of("fifi", "abdo")));
        CollectionXmlEncoderSerdeTest.NestedCubicTime expected =
            new CollectionXmlEncoderSerdeTest.NestedCubicTime(cubic, "fayrouz");
        System.out.println(expected +" ++ "+ actual);
        assertEquals(expected, actual);
    }

    @Test
    void testNestedApiResponseRoundTrip() throws Exception {
        ApiResponse bean = new ApiResponse(List.of(new Dummy("Xyz")));
        Argument<ApiResponse> argument = Argument.of(ApiResponse.class, Argument.listOf(Dummy.class));

        String result = writeXml(mapper, argument, bean);

        assertEquals(
            "<ApiResponse><content><content><name>Xyz</name></content></content></ApiResponse>",
            result
        );

        ApiResponse readBean = mapper.readValue(result, argument);
        System.out.println(readBean.content.getClass());
        assertEquals(1, readBean.content().size());
        assertEquals("Xyz", readBean.content().get(0).name());
    }

//    @Test
//    void testDecodeUsers2() throws Exception {
//        String xml = "<Users2><users><users><_id>39771757156730064829</_id><index>1031703887</index><guid>ifhsrU6geU4PijjDE8Q5</guid><isActive>false</isActive><balance>TKl0GcwTs72S4CPx5rfg</balance><picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture><age>5</age><eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor><name>XjXrEZMuTvPnuOPBg7hL</name><gender>VaMcuWBHvnWvIlCC9q4T</gender><company>6pmCe1LxouRGfZD79ena</company><email>TboNtpmAS0ppZ07jITFE</email><phone>j8OoUhtmwBlI20EgD1LS</phone><address>Aqo4fSYBpvvAWTDqbFbK</address><about>1kXFSA2782BLqNBbKIbp</about><registered>Mc7h3gZJcQ11ShGQYdXI</registered></users></users></Users2>";
//        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
//
//        var actual = mapper.readValue(xmlBytes, io.micronaut.serde.data.Users2.class);
//
//        io.micronaut.serde.data.Users2.User user = new io.micronaut.serde.data.Users2.User();
//        user._id = "39771757156730064829";
//        user.index = 1031703887;
//        user.guid = "ifhsrU6geU4PijjDE8Q5";
//        user.isActive = false;
//        user.balance = "TKl0GcwTs72S4CPx5rfg";
//        user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//        user.age = 5;
//        user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//        user.name = "XjXrEZMuTvPnuOPBg7hL";
//        user.gender = "VaMcuWBHvnWvIlCC9q4T";
//        user.company = "6pmCe1LxouRGfZD79ena";
//        user.email = "TboNtpmAS0ppZ07jITFE";
//        user.phone = "j8OoUhtmwBlI20EgD1LS";
//        user.address = "Aqo4fSYBpvvAWTDqbFbK";
//        user.about = "1kXFSA2782BLqNBbKIbp";
//        user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//
//        io.micronaut.serde.data.Users2 expected = new io.micronaut.serde.data.Users2();
//        expected.users = List.of(user);
//        System.out.println("Expected: " + expected);
//        System.out.println("Actual: " + mapper.writeValueAsString(actual));
//        assertEquals(expected.users.size(), actual.users.size());
//        // If Users2 implements equals/hashCode correctly, you can simply:
//        // assertEquals(expected, actual);
//        assertEquals(expected.users.get(0)._id, actual.users.get(0)._id);
//        assertEquals(expected.users.get(0).index, actual.users.get(0).index);
//        assertEquals(expected.users.get(0).guid, actual.users.get(0).guid);
//        assertEquals(expected.users.get(0).isActive, actual.users.get(0).isActive);
//        assertEquals(expected.users.get(0).balance, actual.users.get(0).balance);
//        assertEquals(expected.users.get(0).picture, actual.users.get(0).picture);
//        assertEquals(expected.users.get(0).age, actual.users.get(0).age);
//        assertEquals(expected.users.get(0).eyeColor, actual.users.get(0).eyeColor);
//        assertEquals(expected.users.get(0).name, actual.users.get(0).name);
//        assertEquals(expected.users.get(0).gender, actual.users.get(0).gender);
//        assertEquals(expected.users.get(0).company, actual.users.get(0).company);
//        assertEquals(expected.users.get(0).email, actual.users.get(0).email);
//        assertEquals(expected.users.get(0).phone, actual.users.get(0).phone);
//        assertEquals(expected.users.get(0).address, actual.users.get(0).address);
//        assertEquals(expected.users.get(0).about, actual.users.get(0).about);
//        assertEquals(expected.users.get(0).registered, actual.users.get(0).registered);
//    }

//    @Test
//    void testDecodeUsers3Full() throws Exception {
//        String xml =
//            "<Users3>" +
//                "<users>" +
//                "<users>" +
//                "<_id>39771757156730064829</_id>" +
//                "<index>1031703887</index>" +
//                "<guid>ifhsrU6geU4PijjDE8Q5</guid>" +
//                "<isActive>false</isActive>" +
//                "<balance>TKl0GcwTs72S4CPx5rfg</balance>" +
//                "<picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture>" +
//                "<age>5</age>" +
//                "<eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor>" +
//                "<name>XjXrEZMuTvPnuOPBg7hL</name>" +
//                "<gender>VaMcuWBHvnWvIlCC9q4T</gender>" +
//                "<company>6pmCe1LxouRGfZD79ena</company>" +
//                "<email>TboNtpmAS0ppZ07jITFE</email>" +
//                "<phone>j8OoUhtmwBlI20EgD1LS</phone>" +
//                "<address>Aqo4fSYBpvvAWTDqbFbK</address>" +
//                "<about>1kXFSA2782BLqNBbKIbp</about>" +
//                "<registered>Mc7h3gZJcQ11ShGQYdXI</registered>" +
//                "<latitude>13.474549605725421</latitude>" +
//                "<longitude>35.010833129741435</longitude>" +
//                "<tags>" +
//                "<tags>8tGfPhZkZD</tags>" +
//                "<tags>XYmwuAAtZ4</tags>" +
//                "<tags>u9iBDMpS9G</tags>" +
//                "<tags>4udy1eRqme</tags>" +
//                "<tags>Lg48Ogrf0I</tags>" +
//                "<tags>zku019kVpo</tags>" +
//                "<tags>iuIMkiZzog</tags>" +
//                "<tags>MuI1uYeCjc</tags>" +
//                "<tags>49n7qisFD8</tags>" +
//                "<tags>TtVgWerCRh</tags>" +
//                "<tags>H604QRJmi1</tags>" +
//                "<tags>ZIQMfqInNH</tags>" +
//                "<tags>CbDyjjA19F</tags>" +
//                "<tags>pNFwPdkVdU</tags>" +
//                "<tags>aPFLsUbIUh</tags>" +
//                "<tags>fA735PT0Hd</tags>" +
//                "<tags>00etYDYL87</tags>" +
//                "<tags>mlyEf1lI2B</tags>" +
//                "<tags>RQ05IJSzXF</tags>" +
//                "<tags>3jJt0Zrkhw</tags>" +
//                "<tags>ZINP8GH4Bm</tags>" +
//                "<tags>XebX8UvviN</tags>" +
//                "<tags>EXqZ9G0ATB</tags>" +
//                "<tags>ssyzWZVAa2</tags>" +
//                "</tags>" +
//                "<friends>" +
//                "<friends><id>2668</id><name>lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe</name></friends>" +
//                "<friends><id>9395</id><name>dxNBbezfkbotyCmFzjodONShlGFaAg</name></friends>" +
//                "<friends><id>5249</id><name>fYHSDXScMSzQvxzFuuPHYWfyjdGQLg</name></friends>" +
//                "<friends><id>4978</id><name>qfoxPWmoWUyUduVkRwhzyBusuflrFY</name></friends>" +
//                "<friends><id>9710</id><name>vUAJwshFGLoBHfwLcsEVNLJLwdaCAg</name></friends>" +
//                "<friends><id>7404</id><name>BhVMdvhPRdpwpDWAmfhNDikncdNgGr</name></friends>" +
//                "<friends><id>1343</id><name>ZeDoizPcOBafZtVYDOmpzGoHekfoxf</name></friends>" +
//                "<friends><id>7382</id><name>KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG</name></friends>" +
//                "<friends><id>1365</id><name>rCSTlgbmTAFhbSfPmnftcDLwdiKsHt</name></friends>" +
//                "<friends><id>8037</id><name>PUvwVYoSvSTnwjJCQITTcwNvMOpxie</name></friends>" +
//                "<friends><id>4858</id><name>cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG</name></friends>" +
//                "<friends><id>9141</id><name>rJxMGOWRjdkphthcaKTspFrMcvcLLb</name></friends>" +
//                "<friends><id>9128</id><name>gcsYaolAQqrNMQTluIAKOkwYTWVUXe</name></friends>" +
//                "<friends><id>2268</id><name>jwXOUcXAiLurRlgTdxyKWvsbNHfFxl</name></friends>" +
//                "<friends><id>5447</id><name>whivfJXOdxoHtLIGpytTdbOXxlZpUY</name></friends>" +
//                "<friends><id>7551</id><name>whykuIjZUgvOFGpmNHjoPeTeYCPNby</name></friends>" +
//                "<friends><id>719</id><name>SmbiwQaORLdsbAlUZbQwgCKfuoPLVr</name></friends>" +
//                "<friends><id>7773</id><name>LZmRMXmXXHzlzFFJAopDNnWkuBqndD</name></friends>" +
//                "<friends><id>9602</id><name>xCNsDBFMygEwZuecJKTUrqeDLBJlrR</name></friends>" +
//                "<friends><id>1536</id><name>hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB</name></friends>" +
//                "<friends><id>3549</id><name>NvvhXwWgCSaYijqhxsrxIWrHbBOOIa</name></friends>" +
//                "</friends>" +
//                "</users>" +
//                "</users>" +
//                "</Users3>";
//
//        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
//        Users3 actual = mapper.readValue(xmlBytes, Users3.class);
//
//        // build expected Users3 object (same as in serializesDeepStructureUsers3Full)
//        Users3.User user = new Users3.User();
//        user._id = "39771757156730064829";
//        user.index = 1031703887;
//        user.guid = "ifhsrU6geU4PijjDE8Q5";
//        user.isActive = false;
//        user.balance = "TKl0GcwTs72S4CPx5rfg";
//        user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//        user.age = 5;
//        user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//        user.name = "XjXrEZMuTvPnuOPBg7hL";
//        user.gender = "VaMcuWBHvnWvIlCC9q4T";
//        user.company = "6pmCe1LxouRGfZD79ena";
//        user.email = "TboNtpmAS0ppZ07jITFE";
//        user.phone = "j8OoUhtmwBlI20EgD1LS";
//        user.address = "Aqo4fSYBpvvAWTDqbFbK";
//        user.about = "1kXFSA2782BLqNBbKIbp";
//        user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//        user.latitude = 13.474549605725421;
//        user.longitude = 35.010833129741435;
//
//        user.tags = List.of(
//            "8tGfPhZkZD", "XYmwuAAtZ4", "u9iBDMpS9G", "4udy1eRqme",
//            "Lg48Ogrf0I", "zku019kVpo", "iuIMkiZzog", "MuI1uYeCjc",
//            "49n7qisFD8", "TtVgWerCRh", "H604QRJmi1", "ZIQMfqInNH",
//            "CbDyjjA19F", "pNFwPdkVdU", "aPFLsUbIUh", "fA735PT0Hd",
//            "00etYDYL87", "mlyEf1lI2B", "RQ05IJSzXF", "3jJt0Zrkhw",
//            "ZINP8GH4Bm", "XebX8UvviN", "EXqZ9G0ATB", "ssyzWZVAa2"
//        );
//
//        Users3.Friend f1  = new Users3.Friend(); f1.id = "2668"; f1.name = "lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe";
//        Users3.Friend f2  = new Users3.Friend(); f2.id = "9395"; f2.name = "dxNBbezfkbotyCmFzjodONShlGFaAg";
//        Users3.Friend f3  = new Users3.Friend(); f3.id = "5249"; f3.name = "fYHSDXScMSzQvxzFuuPHYWfyjdGQLg";
//        Users3.Friend f4  = new Users3.Friend(); f4.id = "4978"; f4.name = "qfoxPWmoWUyUduVkRwhzyBusuflrFY";
//        Users3.Friend f5  = new Users3.Friend(); f5.id = "9710"; f5.name = "vUAJwshFGLoBHfwLcsEVNLJLwdaCAg";
//        Users3.Friend f6  = new Users3.Friend(); f6.id = "7404"; f6.name = "BhVMdvhPRdpwpDWAmfhNDikncdNgGr";
//        Users3.Friend f7  = new Users3.Friend(); f7.id = "1343"; f7.name = "ZeDoizPcOBafZtVYDOmpzGoHekfoxf";
//        Users3.Friend f8  = new Users3.Friend(); f8.id = "7382"; f8.name = "KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG";
//        Users3.Friend f9  = new Users3.Friend(); f9.id = "1365"; f9.name = "rCSTlgbmTAFhbSfPmnftcDLwdiKsHt";
//        Users3.Friend f10 = new Users3.Friend(); f10.id = "8037"; f10.name = "PUvwVYoSvSTnwjJCQITTcwNvMOpxie";
//        Users3.Friend f11 = new Users3.Friend(); f11.id = "4858"; f11.name = "cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG";
//        Users3.Friend f12 = new Users3.Friend(); f12.id = "9141"; f12.name = "rJxMGOWRjdkphthcaKTspFrMcvcLLb";
//        Users3.Friend f13 = new Users3.Friend(); f13.id = "9128"; f13.name = "gcsYaolAQqrNMQTluIAKOkwYTWVUXe";
//        Users3.Friend f14 = new Users3.Friend(); f14.id = "2268"; f14.name = "jwXOUcXAiLurRlgTdxyKWvsbNHfFxl";
//        Users3.Friend f15 = new Users3.Friend(); f15.id = "5447"; f15.name = "whivfJXOdxoHtLIGpytTdbOXxlZpUY";
//        Users3.Friend f16 = new Users3.Friend(); f16.id = "7551"; f16.name = "whykuIjZUgvOFGpmNHjoPeTeYCPNby";
//        Users3.Friend f17 = new Users3.Friend(); f17.id = "719";  f17.name = "SmbiwQaORLdsbAlUZbQwgCKfuoPLVr";
//        Users3.Friend f18 = new Users3.Friend(); f18.id = "7773"; f18.name = "LZmRMXmXXHzlzFFJAopDNnWkuBqndD";
//        Users3.Friend f19 = new Users3.Friend(); f19.id = "9602"; f19.name = "xCNsDBFMygEwZuecJKTUrqeDLBJlrR";
//        Users3.Friend f20 = new Users3.Friend(); f20.id = "1536"; f20.name = "hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB";
//        Users3.Friend f21 = new Users3.Friend(); f21.id = "3549"; f21.name = "NvvhXwWgCSaYijqhxsrxIWrHbBOOIa";
//
//        user.friends = List.of(
//            f1, f2, f3, f4, f5, f6, f7, f8, f9, f10,
//            f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21
//        );
//
//        Users3 expected = new Users3();
//        expected.users = List.of(user);
//
//        // Compare
//        assertEquals(1, actual.users.size());
//        Users3.User actualUser = actual.users.get(0);
//
//        assertEquals(user._id, actualUser._id);
//        assertEquals(user.index, actualUser.index);
//        assertEquals(user.guid, actualUser.guid);
//        assertEquals(user.isActive, actualUser.isActive);
//        assertEquals(user.balance, actualUser.balance);
//        assertEquals(user.picture, actualUser.picture);
//        assertEquals(user.age, actualUser.age);
//        assertEquals(user.eyeColor, actualUser.eyeColor);
//        assertEquals(user.name, actualUser.name);
//        assertEquals(user.gender, actualUser.gender);
//        assertEquals(user.company, actualUser.company);
//        assertEquals(user.email, actualUser.email);
//        assertEquals(user.phone, actualUser.phone);
//        assertEquals(user.address, actualUser.address);
//        assertEquals(user.about, actualUser.about);
//        assertEquals(user.registered, actualUser.registered);
//        assertEquals(user.latitude, actualUser.latitude, 0.000000000000001);
//        assertEquals(user.longitude, actualUser.longitude, 0.000000000000001);
//
//        assertEquals(user.tags, actualUser.tags);
//        assertEquals(user.friends.size(), actualUser.friends.size());
//        for (int i = 0; i < user.friends.size(); i++) {
//            assertEquals(user.friends.get(i).id, actualUser.friends.get(i).id);
//            assertEquals(user.friends.get(i).name, actualUser.friends.get(i).name);
//        }
//    }
//

    @Test
    void testDecodeObjectWithArray() throws Exception {
        String xml =
            "<ObjectWithArray>" +
                "<vals>" +
                    "<vals>" +
                        "<val>foo</val>" +
                    "</vals>" +
                    "<vals>" +
                        "<val>bar</val>" +
                    "</vals>" +
                "</vals>" +
            "</ObjectWithArray>";

        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, ObjectWithArray.class);

        SomeObject o1 = new SomeObject();
        o1.setVal("foo");
        SomeObject o2 = new SomeObject();
        o2.setVal("bar");
        ObjectWithArray expected = new ObjectWithArray();
        expected.setVals(List.of(o1, o2));

        assertEquals(2, actual.getVals().size());
        assertEquals("foo", actual.getVals().get(0).getVal());
        assertEquals("bar", actual.getVals().get(1).getVal());
    }

    @Test
    void testDecodeObjectWithArrayOfArray() throws Exception {
        String xml =
            "<ObjectWithArrayOfArray>" +
                "<vals>" +
                    "<SomeObject>" +                        // first inner list
                        "<SomeObject>" +
                            "<val>foo</val>" +
                        "</SomeObject>" +
                        "<SomeObject>" +
                            "<val>bar</val>" +
                        "</SomeObject>" +
                    "</SomeObject>" +
                    "<SomeObject>" +                        // second inner list
                        "<SomeObject>" +
                            "<val>baz</val>" +
                        "</SomeObject>" +
                    "</SomeObject>" +
                "</vals>" +
            "</ObjectWithArrayOfArray>";

        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        var actual = mapper.readValue(xmlBytes, ObjectWithArrayOfArray.class);

        // [[foo, bar], [baz]]
        assertEquals(2, actual.getVals().size());
        assertEquals(2, actual.getVals().get(0).size());
        assertEquals(1, actual.getVals().get(1).size());

        assertEquals("foo", actual.getVals().get(0).get(0).getVal());
        assertEquals("bar", actual.getVals().get(0).get(1).getVal());
        assertEquals("baz", actual.getVals().get(1).get(0).getVal());
    }


    // null handling :

    @Test
    void missingList() throws Exception {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            String xml = "<ObjectWithArray/>";

            ObjectWithArray obj = xmlMapper.readValue(xml, ObjectWithArray.class);
            System.out.println("missingList => " + obj);

            assertNotNull(obj);
            assertNull(obj.getVals());
        }
    }

    @Test
    void missingListConstructor() throws Exception {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            String xml = "<ObjectWithArrayConstructor></ObjectWithArrayConstructor>";

            ObjectWithArrayConstructor obj =
                xmlMapper.readValue(xml, Argument.of(ObjectWithArrayConstructor.class));
            System.out.println("missingListConstructor => " + obj);

            assertNotNull(obj);
            System.out.println("missingListConstructor => " + obj.toString());
            assertNull(obj.getVals());
        }
    }

    @Test
    void missingListRecord() throws Exception {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            String xml = "<ObjectWithArrayRecord />";

            ObjectWithArrayRecord obj =
                xmlMapper.readValue(xml, Argument.of(ObjectWithArrayRecord.class));
            System.out.println("missingListRecord => " + obj);

            assertNotNull(obj);
            assertNull(obj.vals());
        }
    }

    @Test
    void missingListRequired() throws Exception {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            String xml = "<ObjectWithArrayRequired/>";

            Exception e = assertThrows(Exception.class, () ->
                xmlMapper.readValue(xml, Argument.of(ObjectWithArrayRequired.class))
            );
            String msg = e.getMessage();
            System.out.println("missingListRequired exception => " + msg);

            assertTrue(
                msg.contains("Required constructor parameter") ||
                    msg.contains("Missing required creator property"),
                "Unexpected message: " + msg
            );
        }
    }

//    @Test
//    void testDecodeUsers3StrippedExtraFieldsIgnored() throws Exception {
//        String xml =
//            "<Users3>" +
//                "<users>" +
//                "<users>" +
//                "<_id>39771757156730064829</_id>" +
//                "<index>1031703887</index>" +
//                "<guid>ifhsrU6geU4PijjDE8Q5</guid>" +
//                "<isActive>false</isActive>" +
//                "<balance>TKl0GcwTs72S4CPx5rfg</balance>" +
//                "<picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture>" +
//                "<age>5</age>" +
//                "<eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor>" +
//                "<name>XjXrEZMuTvPnuOPBg7hL</name>" +
//                "<gender>VaMcuWBHvnWvIlCC9q4T</gender>" +
//                "<company>6pmCe1LxouRGfZD79ena</company>" +
//                "<email>TboNtpmAS0ppZ07jITFE</email>" +
//                "<phone>j8OoUhtmwBlI20EgD1LS</phone>" +
//                "<address>Aqo4fSYBpvvAWTDqbFbK</address>" +
//                "<about>1kXFSA2782BLqNBbKIbp</about>" +
//                "<registered>Mc7h3gZJcQ11ShGQYdXI</registered>" +
//                "<latitude>13.474549605725421</latitude>" +
//                "<longitude>35.010833129741435</longitude>" +
//                "<tags>" +
//                "<tags>8tGfPhZkZD</tags>" +
//                "<tags>XYmwuAAtZ4</tags>" +
//                "<tags>u9iBDMpS9G</tags>" +
//                "<tags>4udy1eRqme</tags>" +
//                "<tags>Lg48Ogrf0I</tags>" +
//                "<tags>zku019kVpo</tags>" +
//                "<tags>iuIMkiZzog</tags>" +
//                "<tags>MuI1uYeCjc</tags>" +
//                "<tags>49n7qisFD8</tags>" +
//                "<tags>TtVgWerCRh</tags>" +
//                "<tags>H604QRJmi1</tags>" +
//                "<tags>ZIQMfqInNH</tags>" +
//                "<tags>CbDyjjA19F</tags>" +
//                "<tags>pNFwPdkVdU</tags>" +
//                "<tags>aPFLsUbIUh</tags>" +
//                "<tags>fA735PT0Hd</tags>" +
//                "<tags>00etYDYL87</tags>" +
//                "<tags>mlyEf1lI2B</tags>" +
//                "<tags>RQ05IJSzXF</tags>" +
//                "<tags>3jJt0Zrkhw</tags>" +
//                "<tags>ZINP8GH4Bm</tags>" +
//                "<tags>XebX8UvviN</tags>" +
//                "<tags>EXqZ9G0ATB</tags>" +
//                "<tags>ssyzWZVAa2</tags>" +
//                "</tags>" +
//                "<friends>" +
//                "<friends><id>2668</id><name>lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe</name></friends>" +
//                "<friends><id>9395</id><name>dxNBbezfkbotyCmFzjodONShlGFaAg</name></friends>" +
//                "<friends><id>5249</id><name>fYHSDXScMSzQvxzFuuPHYWfyjdGQLg</name></friends>" +
//                "<friends><id>4978</id><name>qfoxPWmoWUyUduVkRwhzyBusuflrFY</name></friends>" +
//                "<friends><id>9710</id><name>vUAJwshFGLoBHfwLcsEVNLJLwdaCAg</name></friends>" +
//                "<friends><id>7404</id><name>BhVMdvhPRdpwpDWAmfhNDikncdNgGr</name></friends>" +
//                "<friends><id>1343</id><name>ZeDoizPcOBafZtVYDOmpzGoHekfoxf</name></friends>" +
//                "<friends><id>7382</id><name>KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG</name></friends>" +
//                "<friends><id>1365</id><name>rCSTlgbmTAFhbSfPmnftcDLwdiKsHt</name></friends>" +
//                "<friends><id>8037</id><name>PUvwVYoSvSTnwjJCQITTcwNvMOpxie</name></friends>" +
//                "<friends><id>4858</id><name>cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG</name></friends>" +
//                "<friends><id>9141</id><name>rJxMGOWRjdkphthcaKTspFrMcvcLLb</name></friends>" +
//                "<friends><id>9128</id><name>gcsYaolAQqrNMQTluIAKOkwYTWVUXe</name></friends>" +
//                "<friends><id>2268</id><name>jwXOUcXAiLurRlgTdxyKWvsbNHfFxl</name></friends>" +
//                "<friends><id>5447</id><name>whivfJXOdxoHtLIGpytTdbOXxlZpUY</name></friends>" +
//                "<friends><id>7551</id><name>whykuIjZUgvOFGpmNHjoPeTeYCPNby</name></friends>" +
//                "<friends><id>719</id><name>SmbiwQaORLdsbAlUZbQwgCKfuoPLVr</name></friends>" +
//                "<friends><id>7773</id><name>LZmRMXmXXHzlzFFJAopDNnWkuBqndD</name></friends>" +
//                "<friends><id>9602</id><name>xCNsDBFMygEwZuecJKTUrqeDLBJlrR</name></friends>" +
//                "<friends><id>1536</id><name>hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB</name></friends>" +
//                "<friends><id>3549</id><name>NvvhXwWgCSaYijqhxsrxIWrHbBOOIa</name></friends>" +
//                "</friends>" +
//                // extra fields that are NOT present on Users3.User
//                "<greeting>hTAIJLspvLr8DJPG3jYh</greeting>" +
//                "<favoriteFruit>f6ZsZ3saRGKMBCZLAkiP</favoriteFruit>" +
//                "</users>" +
//                "</users>" +
//                "</Users3>";
//
//        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
//        Users3 actual = mapper.readValue(xmlBytes, Users3.class);
//
//        // Build expected object (without greeting/favoriteFruit, since Users3.User has no such fields)
//        Users3.User user = new Users3.User();
//        user._id = "39771757156730064829";
//        user.index = 1031703887;
//        user.guid = "ifhsrU6geU4PijjDE8Q5";
//        user.isActive = false;
//        user.balance = "TKl0GcwTs72S4CPx5rfg";
//        user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//        user.age = 5;
//        user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//        user.name = "XjXrEZMuTvPnuOPBg7hL";
//        user.gender = "VaMcuWBHvnWvIlCC9q4T";
//        user.company = "6pmCe1LxouRGfZD79ena";
//        user.email = "TboNtpmAS0ppZ07jITFE";
//        user.phone = "j8OoUhtmwBlI20EgD1LS";
//        user.address = "Aqo4fSYBpvvAWTDqbFbK";
//        user.about = "1kXFSA2782BLqNBbKIbp";
//        user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//        user.latitude = 13.474549605725421;
//        user.longitude = 35.010833129741435;
//
//        user.tags = List.of(
//            "8tGfPhZkZD", "XYmwuAAtZ4", "u9iBDMpS9G", "4udy1eRqme",
//            "Lg48Ogrf0I", "zku019kVpo", "iuIMkiZzog", "MuI1uYeCjc",
//            "49n7qisFD8", "TtVgWerCRh", "H604QRJmi1", "ZIQMfqInNH",
//            "CbDyjjA19F", "pNFwPdkVdU", "aPFLsUbIUh", "fA735PT0Hd",
//            "00etYDYL87", "mlyEf1lI2B", "RQ05IJSzXF", "3jJt0Zrkhw",
//            "ZINP8GH4Bm", "XebX8UvviN", "EXqZ9G0ATB", "ssyzWZVAa2"
//        );
//
//        Users3.Friend f1  = new Users3.Friend(); f1.id = "2668"; f1.name = "lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe";
//        Users3.Friend f2  = new Users3.Friend(); f2.id = "9395"; f2.name = "dxNBbezfkbotyCmFzjodONShlGFaAg";
//        Users3.Friend f3  = new Users3.Friend(); f3.id = "5249"; f3.name = "fYHSDXScMSzQvxzFuuPHYWfyjdGQLg";
//        Users3.Friend f4  = new Users3.Friend(); f4.id = "4978"; f4.name = "qfoxPWmoWUyUduVkRwhzyBusuflrFY";
//        Users3.Friend f5  = new Users3.Friend(); f5.id = "9710"; f5.name = "vUAJwshFGLoBHfwLcsEVNLJLwdaCAg";
//        Users3.Friend f6  = new Users3.Friend(); f6.id = "7404"; f6.name = "BhVMdvhPRdpwpDWAmfhNDikncdNgGr";
//        Users3.Friend f7  = new Users3.Friend(); f7.id = "1343"; f7.name = "ZeDoizPcOBafZtVYDOmpzGoHekfoxf";
//        Users3.Friend f8  = new Users3.Friend(); f8.id = "7382"; f8.name = "KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG";
//        Users3.Friend f9  = new Users3.Friend(); f9.id = "1365"; f9.name = "rCSTlgbmTAFhbSfPmnftcDLwdiKsHt";
//        Users3.Friend f10 = new Users3.Friend(); f10.id = "8037"; f10.name = "PUvwVYoSvSTnwjJCQITTcwNvMOpxie";
//        Users3.Friend f11 = new Users3.Friend(); f11.id = "4858"; f11.name = "cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG";
//        Users3.Friend f12 = new Users3.Friend(); f12.id = "9141"; f12.name = "rJxMGOWRjdkphthcaKTspFrMcvcLLb";
//        Users3.Friend f13 = new Users3.Friend(); f13.id = "9128"; f13.name = "gcsYaolAQqrNMQTluIAKOkwYTWVUXe";
//        Users3.Friend f14 = new Users3.Friend(); f14.id = "2268"; f14.name = "jwXOUcXAiLurRlgTdxyKWvsbNHfFxl";
//        Users3.Friend f15 = new Users3.Friend(); f15.id = "5447"; f15.name = "whivfJXOdxoHtLIGpytTdbOXxlZpUY";
//        Users3.Friend f16 = new Users3.Friend(); f16.id = "7551"; f16.name = "whykuIjZUgvOFGpmNHjoPeTeYCPNby";
//        Users3.Friend f17 = new Users3.Friend(); f17.id = "719";  f17.name = "SmbiwQaORLdsbAlUZbQwgCKfuoPLVr";
//        Users3.Friend f18 = new Users3.Friend(); f18.id = "7773"; f18.name = "LZmRMXmXXHzlzFFJAopDNnWkuBqndD";
//        Users3.Friend f19 = new Users3.Friend(); f19.id = "9602"; f19.name = "xCNsDBFMygEwZuecJKTUrqeDLBJlrR";
//        Users3.Friend f20 = new Users3.Friend(); f20.id = "1536"; f20.name = "hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB";
//        Users3.Friend f21 = new Users3.Friend(); f21.id = "3549"; f21.name = "NvvhXwWgCSaYijqhxsrxIWrHbBOOIa";
//
//        user.friends = List.of(
//            f1, f2, f3, f4, f5, f6, f7, f8, f9, f10,
//            f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21
//        );
//
//        Users3 expected = new Users3();
//        expected.users = List.of(user);
//
//        // Assertions: all known fields match, extra fields caused no error and did not map anywhere
//        assertEquals(1, actual.users.size());
//        Users3.User actualUser = actual.users.get(0);
//
//        assertEquals(user._id, actualUser._id);
//        assertEquals(user.index, actualUser.index);
//        assertEquals(user.guid, actualUser.guid);
//        assertEquals(user.isActive, actualUser.isActive);
//        assertEquals(user.balance, actualUser.balance);
//        assertEquals(user.picture, actualUser.picture);
//        assertEquals(user.age, actualUser.age);
//        assertEquals(user.eyeColor, actualUser.eyeColor);
//        assertEquals(user.name, actualUser.name);
//        assertEquals(user.gender, actualUser.gender);
//        assertEquals(user.company, actualUser.company);
//        assertEquals(user.email, actualUser.email);
//        assertEquals(user.phone, actualUser.phone);
//        assertEquals(user.address, actualUser.address);
//        assertEquals(user.about, actualUser.about);
//        assertEquals(user.registered, actualUser.registered);
//        assertEquals(user.latitude, actualUser.latitude, 1e-15);
//        assertEquals(user.longitude, actualUser.longitude, 1e-15);
//
//        assertEquals(user.tags, actualUser.tags);
//        assertEquals(user.friends.size(), actualUser.friends.size());
//        for (int i = 0; i < user.friends.size(); i++) {
//            assertEquals(user.friends.get(i).id, actualUser.friends.get(i).id);
//            assertEquals(user.friends.get(i).name, actualUser.friends.get(i).name);
//        }
//    }

//    @Test
//    void testDecodeUsers2WithExtraFieldsIgnored() throws Exception {
//        // XML equivalent of the 'json' example (full with extra fields)
//        String xml =
//            "<Users2>" +
//                "<users>" +
//                "<users>" +
//                "<_id>39771757156730064829</_id>" +
//                "<index>1031703887</index>" +
//                "<guid>ifhsrU6geU4PijjDE8Q5</guid>" +
//                "<isActive>false</isActive>" +
//                "<balance>TKl0GcwTs72S4CPx5rfg</balance>" +
//                "<picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture>" +
//                "<age>5</age>" +
//                "<eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor>" +
//                "<name>XjXrEZMuTvPnuOPBg7hL</name>" +
//                "<gender>VaMcuWBHvnWvIlCC9q4T</gender>" +
//                "<company>6pmCe1LxouRGfZD79ena</company>" +
//                "<email>TboNtpmAS0ppZ07jITFE</email>" +
//                "<phone>j8OoUhtmwBlI20EgD1LS</phone>" +
//                "<address>Aqo4fSYBpvvAWTDqbFbK</address>" +
//                "<about>1kXFSA2782BLqNBbKIbp</about>" +
//                "<registered>Mc7h3gZJcQ11ShGQYdXI</registered>" +
//                // extra fields not present in Users2.User
//                "<latitude>13.474549605725421</latitude>" +
//                "<longitude>35.010833129741435</longitude>" +
//                "<tags>" +
//                "<tags>8tGfPhZkZD</tags>" +
//                "<tags>XYmwuAAtZ4</tags>" +
//                "<tags>u9iBDMpS9G</tags>" +
//                "<tags>4udy1eRqme</tags>" +
//                "<tags>Lg48Ogrf0I</tags>" +
//                "<tags>zku019kVpo</tags>" +
//                "<tags>iuIMkiZzog</tags>" +
//                "<tags>MuI1uYeCjc</tags>" +
//                "<tags>49n7qisFD8</tags>" +
//                "<tags>TtVgWerCRh</tags>" +
//                "<tags>H604QRJmi1</tags>" +
//                "<tags>ZIQMfqInNH</tags>" +
//                "<tags>CbDyjjA19F</tags>" +
//                "<tags>pNFwPdkVdU</tags>" +
//                "<tags>aPFLsUbIUh</tags>" +
//                "<tags>fA735PT0Hd</tags>" +
//                "<tags>00etYDYL87</tags>" +
//                "<tags>mlyEf1lI2B</tags>" +
//                "<tags>RQ05IJSzXF</tags>" +
//                "<tags>3jJt0Zrkhw</tags>" +
//                "<tags>ZINP8GH4Bm</tags>" +
//                "<tags>XebX8UvviN</tags>" +
//                "<tags>EXqZ9G0ATB</tags>" +
//                "<tags>ssyzWZVAa2</tags>" +
//                "</tags>" +
//                "<friends>" +
//                "<friends><id>2668</id><name>lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe</name></friends>" +
//                "<friends><id>9395</id><name>dxNBbezfkbotyCmFzjodONShlGFaAg</name></friends>" +
//                "<friends><id>5249</id><name>fYHSDXScMSzQvxzFuuPHYWfyjdGQLg</name></friends>" +
//                "<friends><id>4978</id><name>qfoxPWmoWUyUduVkRwhzyBusuflrFY</name></friends>" +
//                "<friends><id>9710</id><name>vUAJwshFGLoBHfwLcsEVNLJLwdaCAg</name></friends>" +
//                "<friends><id>7404</id><name>BhVMdvhPRdpwpDWAmfhNDikncdNgGr</name></friends>" +
//                "<friends><id>1343</id><name>ZeDoizPcOBafZtVYDOmpzGoHekfoxf</name></friends>" +
//                "<friends><id>7382</id><name>KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG</name></friends>" +
//                "<friends><id>1365</id><name>rCSTlgbmTAFhbSfPmnftcDLwdiKsHt</name></friends>" +
//                "<friends><id>8037</id><name>PUvwVYoSvSTnwjJCQITTcwNvMOpxie</name></friends>" +
//                "<friends><id>4858</id><name>cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG</name></friends>" +
//                "<friends><id>9141</id><name>rJxMGOWRjdkphthcaKTspFrMcvcLLb</name></friends>" +
//                "<friends><id>9128</id><name>gcsYaolAQqrNMQTluIAKOkwYTWVUXe</name></friends>" +
//                "<friends><id>2268</id><name>jwXOUcXAiLurRlgTdxyKWvsbNHfFxl</name></friends>" +
//                "<friends><id>5447</id><name>whivfJXOdxoHtLIGpytTdbOXxlZpUY</name></friends>" +
//                "<friends><id>7551</id><name>whykuIjZUgvOFGpmNHjoPeTeYCPNby</name></friends>" +
//                "<friends><id>719</id><name>SmbiwQaORLdsbAlUZbQwgCKfuoPLVr</name></friends>" +
//                "<friends><id>7773</id><name>LZmRMXmXXHzlzFFJAopDNnWkuBqndD</name></friends>" +
//                "<friends><id>9602</id><name>xCNsDBFMygEwZuecJKTUrqeDLBJlrR</name></friends>" +
//                "<friends><id>1536</id><name>hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB</name></friends>" +
//                "<friends><id>3549</id><name>NvvhXwWgCSaYijqhxsrxIWrHbBOOIa</name></friends>" +
//                "</friends>" +
//                "<greeting>hTAIJLspvLr8DJPG3jYh</greeting>" +
//                "<favoriteFruit>f6ZsZ3saRGKMBCZLAkiP</favoriteFruit>" +
//                "</users>" +
//                "</users>" +
//                "</Users2>";
//
//        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
//        io.micronaut.serde.data.Users2 actual =
//            mapper.readValue(xmlBytes, io.micronaut.serde.data.Users2.class);
//
//        // Expected Users2 only with the fields Users2.User actually defines
//        io.micronaut.serde.data.Users2.User user = new io.micronaut.serde.data.Users2.User();
//        user._id = "39771757156730064829";
//        user.index = 1031703887;
//        user.guid = "ifhsrU6geU4PijjDE8Q5";
//        user.isActive = false;
//        user.balance = "TKl0GcwTs72S4CPx5rfg";
//        user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//        user.age = 5;
//        user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//        user.name = "XjXrEZMuTvPnuOPBg7hL";
//        user.gender = "VaMcuWBHvnWvIlCC9q4T";
//        user.company = "6pmCe1LxouRGfZD79ena";
//        user.email = "TboNtpmAS0ppZ07jITFE";
//        user.phone = "j8OoUhtmwBlI20EgD1LS";
//        user.address = "Aqo4fSYBpvvAWTDqbFbK";
//        user.about = "1kXFSA2782BLqNBbKIbp";
//        user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//
//        io.micronaut.serde.data.Users2 expected = new io.micronaut.serde.data.Users2();
//        expected.users = List.of(user);
//
//        // Assertions: all defined fields match; extra XML caused no failure and did not map anywhere
//        assertEquals(1, actual.users.size());
//        io.micronaut.serde.data.Users2.User actualUser = actual.users.get(0);
//
//        assertEquals(user._id, actualUser._id);
//        assertEquals(user.index, actualUser.index);
//        assertEquals(user.guid, actualUser.guid);
//        assertEquals(user.isActive, actualUser.isActive);
//        assertEquals(user.balance, actualUser.balance);
//        assertEquals(user.picture, actualUser.picture);
//        assertEquals(user.age, actualUser.age);
//        assertEquals(user.eyeColor, actualUser.eyeColor);
//        assertEquals(user.name, actualUser.name);
//        assertEquals(user.gender, actualUser.gender);
//        assertEquals(user.company, actualUser.company);
//        assertEquals(user.email, actualUser.email);
//        assertEquals(user.phone, actualUser.phone);
//        assertEquals(user.address, actualUser.address);
//        assertEquals(user.about, actualUser.about);
//        assertEquals(user.registered, actualUser.registered);
//    }
//

    /**
     * An empty repeated element {@code <vals></vals>} should deserialize as {@code null}
     * rather than crashing with "Expected object but got: VALUE_STRING".
     */
    @Test
    void testDecodeObjectArrayWithNullElement() throws Exception {
        String xml =
            "<ObjectWithArray>" +
                "<vals>" +
                    "<vals><val>foo</val></vals>" +
                    "<vals></vals>" +          // empty element → null
                    "<vals><val>bar</val></vals>" +
                "</vals>" +
            "</ObjectWithArray>";

        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        ObjectWithArray actual = mapper.readValue(xmlBytes, ObjectWithArray.class);

        assertNotNull(actual);
        assertEquals(3, actual.getVals().size());
        assertNotNull(actual.getVals().get(0));
        assertEquals("foo", actual.getVals().get(0).getVal());
        assertNull(actual.getVals().get(1));
        assertNotNull(actual.getVals().get(2));
        assertEquals("bar", actual.getVals().get(2).getVal());
    }

    @Serdeable
    public static final class ObjectWithArrayOfArray {
        private List<List<SomeObject>> vals;

        public List<List<SomeObject>> getVals() {
            return vals;
        }

        public void setVals(List<List<SomeObject>> vals) {
            this.vals = vals;
        }
    }

    @Serdeable
    public static final class ObjectWithArray {

        private List<SomeObject> vals;

        public ObjectWithArray() {
        }

        public List<SomeObject> getVals() {
            return vals;
        }

        public void setVals(List<SomeObject> vals) {
            this.vals = vals;
        }
    }

    @Serdeable
    public static final class SomeObject {

        private String val;

        public SomeObject() {
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }
    }

    @Serdeable
    public static final class ObjectWithArrayConstructor {
        @Nullable
        private final List<SomeObject> vals;

        public ObjectWithArrayConstructor(@Nullable List<SomeObject> vals) {
            this.vals = vals;
        }

        public List<SomeObject> getVals() {
            return vals;
        }

        @Override
        public String toString() {
            return "ObjectWithArrayConstructor{" +
                "vals=" + vals +
                '}';
        }
    }

    @Serdeable
    public static final class ObjectWithArrayRecord {
        private final List<SomeObject> vals;

        public ObjectWithArrayRecord(List<SomeObject> vals) {
            this.vals = vals;
        }

        public List<SomeObject> vals() {
            return vals;
        }

        @Override
        public String toString() {
            return "ObjectWithArrayRecord{" +
                "vals=" + vals +
                '}';
        }
    }

    @Serdeable
    public static final class ObjectWithArrayRequired {
        @JsonProperty(required = true)
        private final List<SomeObject> vals;

        public ObjectWithArrayRequired(@JsonProperty(required = true) List<SomeObject> vals) {
            // model “required” by not providing any default and
            // letting Micronaut treat this as a required ctor arg
            this.vals = vals;
        }

        public List<SomeObject> getVals() {
            return vals;
        }

        @Override
        public String toString() {
            return "ObjectWithArrayRequired{" +
                "vals=" + vals +
                '}';
        }
    }

    @Serdeable
    record ApiResponse(List<Dummy> content) {
    }

    @Serdeable
    record Dummy(String name) {
    }


}
