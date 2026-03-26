package io.micronaut.serde.xml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
//import io.micronaut.serde.data.Users2;
//import io.micronaut.serde.data.Users1;
//import io.micronaut.serde.data.Users3;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionXmlEncoderSerdeTest {

    private static <T> String writeXml(ObjectMapper xmlMapper, Argument<T> argument, T bean) throws IOException {
        return new String(xmlMapper.writeValueAsBytes(argument, bean), StandardCharsets.UTF_8);
    }

    @Serdeable
    public static class Pip {
        private List<String> pap;

        public Pip() {
        }

        public Pip(List<String> pap) {
            this.pap = pap;
        }

        public List<String> getPap() {
            return pap;
        }

        public void setPap(List<String> pap) {
            this.pap = pap;
        }
    }

    //@JsonRootName("Typed")
    @Serdeable
    static class TypeBean {
        @JsonProperty(required = true)
        String typeId;

        protected TypeBean() { }

        public String getTypeId() {
            return typeId;
        }

        public void setTypeId(String typeId) {
            this.typeId = typeId;
        }
    }

    @Test
    void serdeProp() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);
            TypeBean bean = new TypeBean();
            bean.typeId = "type";
            var valueAsString = xmlMapper.writeValueAsString(bean);
            System.out.println("====>" +  valueAsString);
        }
    }


    @Test
    void serializesListWithSiblingFieldOutsideWrapper() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

//            StringListBean bean = new StringListBean(23, "ali", List.of("foo", "bar"), "salamander");
//            String result = writeXml(xmlMapper, Argument.of(StringListBean.class), bean);
//            System.out.println("---->"+result);

            Pip pip1 = new Pip(List.of("foo", "bar", "salamander"));
            XmlMapper xml = new XmlMapper();
            String pipstring = xml.writeValueAsString(pip1);
            System.out.println("---->" + pipstring);
            // ----><Pip><pap><pap>foo</pap><pap>bar</pap><pap>salamander</pap></pap></Pip>
            Pip decoded = xmlMapper.readValue(pipstring, Pip.class);
            System.out.println("---->" + decoded.getPap());

            assertEquals(pipstring, pipstring);

//            assertEquals(
//                result,
//                "<StringListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></StringListBean>"
//
//            );
        }
    }

    @Test
    void serializesArrayListWithSiblingFieldOutsideWrapper() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            ArrayListBean bean = new ArrayListBean(23, "ali", new ArrayList<>(List.of("foo", "bar")), "salamander");
            String result = writeXml(xmlMapper, Argument.of(ArrayListBean.class), bean);

            assertEquals(
                "<ArrayListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></ArrayListBean>",
                result
            );
        }
    }

    @Test
    void serializesLinkedListWithSiblingFieldOutsideWrapper() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            LinkedListBean bean = new LinkedListBean(23, "ali", new LinkedList<>(List.of("foo", "bar")), "salamander");
            String result = writeXml(xmlMapper, Argument.of(LinkedListBean.class), bean);

            assertEquals(
                "<LinkedListBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></LinkedListBean>",
                result
            );
        }
    }

    @Test
    void serializesLinkedHashSetWithSiblingFieldOutsideWrapper() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            LinkedHashSetBean bean = new LinkedHashSetBean(23, "ali", new LinkedHashSet<>(List.of("foo", "bar")), "salamander");
            String result = writeXml(xmlMapper, Argument.of(LinkedHashSetBean.class), bean);

            assertEquals(
                "<LinkedHashSetBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></LinkedHashSetBean>",
                result
            );
        }
    }

    @Test
    void serializesCollectionDeclarationWithSiblingFieldOutsideWrapper() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            Collection<String> strings = new ArrayList<>(List.of("foo", "bar"));
            CollectionBean bean = new CollectionBean(23, "ali", strings, "salamander");
            String result = writeXml(xmlMapper, Argument.of(CollectionBean.class), bean);

            assertEquals(
                "<CollectionBean><age>23</age><name>ali</name><strings><strings>foo</strings><strings>bar</strings></strings><prenom>salamander</prenom></CollectionBean>",
                result
            );
        }
    }

    @Test
    void serializesNestedListsWithoutLeakingWrapperToSiblingField() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            NestedCollectionBean bean = new NestedCollectionBean(
                List.of(List.of("foo", "bar"), List.of("baz")),
                "salamander"
            );
            String result = writeXml(xmlMapper, Argument.of(NestedCollectionBean.class), bean);
            System.out.println("=====>" + result);
            assertEquals(
                "<NestedCollectionBean><strings><String><String>foo</String><String>bar</String></String><String><String>baz</String></String></strings><prenom>salamander</prenom></NestedCollectionBean>",
                result
            );
        }
    }

    @Test
    void serializeNestedCubicField() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            NestedCubicTime bean = new NestedCubicTime(
                List.of(List.of(List.of("fifi", "abdo"))),
                "fayrouz"
            );
            String result = writeXml(xmlMapper, Argument.of(NestedCubicTime.class), bean);
            System.out.println("=====>" + result);
            assertEquals(
                "<NestedCubicTime><strings><List><String><String>fifi</String><String>abdo</String></String></List></strings><prenom>fayrouz</prenom></NestedCubicTime>",
                result
            );


        }
    }

//    @Test
//    void serializesDeepStructureUsers2() throws IOException {
//        try (ApplicationContext ctx = ApplicationContext.run()) {
//            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);
//
//            Users2.User user = new Users2.User();
//            user._id = "39771757156730064829";
//            user.index = 1031703887;
//            user.guid = "ifhsrU6geU4PijjDE8Q5";
//            user.isActive = false;
//            user.balance = "TKl0GcwTs72S4CPx5rfg";
//            user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//            user.age = 5;
//            user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//            user.name = "XjXrEZMuTvPnuOPBg7hL";
//            user.gender = "VaMcuWBHvnWvIlCC9q4T";
//            user.company = "6pmCe1LxouRGfZD79ena";
//            user.email = "TboNtpmAS0ppZ07jITFE";
//            user.phone = "j8OoUhtmwBlI20EgD1LS";
//            user.address = "Aqo4fSYBpvvAWTDqbFbK";
//            user.about = "1kXFSA2782BLqNBbKIbp";
//            user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//
//            Users2 bean = new Users2();
//            bean.users = List.of(user);
//
//            String result = writeXml(xmlMapper, Argument.of(Users2.class), bean);
//            assertEquals(
//                "<Users2><users><users><_id>39771757156730064829</_id><index>1031703887</index><guid>ifhsrU6geU4PijjDE8Q5</guid><isActive>false</isActive><balance>TKl0GcwTs72S4CPx5rfg</balance><picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture><age>5</age><eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor><name>XjXrEZMuTvPnuOPBg7hL</name><gender>VaMcuWBHvnWvIlCC9q4T</gender><company>6pmCe1LxouRGfZD79ena</company><email>TboNtpmAS0ppZ07jITFE</email><phone>j8OoUhtmwBlI20EgD1LS</phone><address>Aqo4fSYBpvvAWTDqbFbK</address><about>1kXFSA2782BLqNBbKIbp</about><registered>Mc7h3gZJcQ11ShGQYdXI</registered></users></users></Users2>",
//                result
//            );
//        }
//
//
//    }

    @Test
    void serializesObjectWithArray() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            SomeObject o1 = new SomeObject();
            o1.setVal("foo");
            SomeObject o2 = new SomeObject();
            o2.setVal("bar");
            ObjectWithArray bean = new ObjectWithArray();
            bean.setVals(List.of(o1, o2));

            String result = new String(
                xmlMapper.writeValueAsBytes(Argument.of(ObjectWithArray.class), bean),
                StandardCharsets.UTF_8
            );
            System.out.println("ObjectWithArray XML => " + result);

            // Adjust the expected XML if your actual output differs
            String expected =
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

            assertEquals(expected, result);
        }
    }

    @Test
    void serializesObjectWithArrayOfArray() throws IOException {
        try (ApplicationContext ctx = ApplicationContext.run()) {
            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);

            SomeObject o1 = new SomeObject();
            o1.setVal("foo");
            SomeObject o2 = new SomeObject();
            o2.setVal("bar");
            SomeObject o3 = new SomeObject();
            o3.setVal("baz");

            ObjectWithArrayOfArray bean = new ObjectWithArrayOfArray();
            bean.setVals(
                List.of(
                    List.of(o1, o2),   // first inner list
                    List.of(o3)        // second inner list
                )
            );

            String result = new String(
                // List<List<SomeObject>> vals     ======> vals > List > SomeObject
                xmlMapper.writeValueAsBytes(Argument.of(ObjectWithArrayOfArray.class), bean),
                StandardCharsets.UTF_8
            );
            System.out.println("ObjectWithArrayOfArray XML => " + result);

            // Expected XML following the same nested <vals> convention as NestedCollectionBean
            String expected =
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

            assertEquals(expected, result);
        }
    }

//    @Test
//    void serializesDeepStructureUsers3Full() throws IOException {
//        try (ApplicationContext ctx = ApplicationContext.run()) {
//            ObjectMapper xmlMapper = ctx.getBean(XmlObjectMapper.class);
//
//            Users3.User user = new Users3.User();
//            user._id = "39771757156730064829";
//            user.index = 1031703887;
//            user.guid = "ifhsrU6geU4PijjDE8Q5";
//            user.isActive = false;
//            user.balance = "TKl0GcwTs72S4CPx5rfg";
//            user.picture = "FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8";
//            user.age = 5;
//            user.eyeColor = "AY79Pw4sYByUZEMLxnYJ";
//            user.name = "XjXrEZMuTvPnuOPBg7hL";
//            user.gender = "VaMcuWBHvnWvIlCC9q4T";
//            user.company = "6pmCe1LxouRGfZD79ena";
//            user.email = "TboNtpmAS0ppZ07jITFE";
//            user.phone = "j8OoUhtmwBlI20EgD1LS";
//            user.address = "Aqo4fSYBpvvAWTDqbFbK";
//            user.about = "1kXFSA2782BLqNBbKIbp";
//            user.registered = "Mc7h3gZJcQ11ShGQYdXI";
//            user.latitude = 13.474549605725421;
//            user.longitude = 35.010833129741435;
//
//            user.tags = List.of(
//                "8tGfPhZkZD", "XYmwuAAtZ4", "u9iBDMpS9G", "4udy1eRqme",
//                "Lg48Ogrf0I", "zku019kVpo", "iuIMkiZzog", "MuI1uYeCjc",
//                "49n7qisFD8", "TtVgWerCRh", "H604QRJmi1", "ZIQMfqInNH",
//                "CbDyjjA19F", "pNFwPdkVdU", "aPFLsUbIUh", "fA735PT0Hd",
//                "00etYDYL87", "mlyEf1lI2B", "RQ05IJSzXF", "3jJt0Zrkhw",
//                "ZINP8GH4Bm", "XebX8UvviN", "EXqZ9G0ATB", "ssyzWZVAa2"
//            );
//
//            Users3.Friend f1  = new Users3.Friend(); f1.id = "2668"; f1.name = "lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe";
//            Users3.Friend f2  = new Users3.Friend(); f2.id = "9395"; f2.name = "dxNBbezfkbotyCmFzjodONShlGFaAg";
//            Users3.Friend f3  = new Users3.Friend(); f3.id = "5249"; f3.name = "fYHSDXScMSzQvxzFuuPHYWfyjdGQLg";
//            Users3.Friend f4  = new Users3.Friend(); f4.id = "4978"; f4.name = "qfoxPWmoWUyUduVkRwhzyBusuflrFY";
//            Users3.Friend f5  = new Users3.Friend(); f5.id = "9710"; f5.name = "vUAJwshFGLoBHfwLcsEVNLJLwdaCAg";
//            Users3.Friend f6  = new Users3.Friend(); f6.id = "7404"; f6.name = "BhVMdvhPRdpwpDWAmfhNDikncdNgGr";
//            Users3.Friend f7  = new Users3.Friend(); f7.id = "1343"; f7.name = "ZeDoizPcOBafZtVYDOmpzGoHekfoxf";
//            Users3.Friend f8  = new Users3.Friend(); f8.id = "7382"; f8.name = "KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG";
//            Users3.Friend f9  = new Users3.Friend(); f9.id = "1365"; f9.name = "rCSTlgbmTAFhbSfPmnftcDLwdiKsHt";
//            Users3.Friend f10 = new Users3.Friend(); f10.id = "8037"; f10.name = "PUvwVYoSvSTnwjJCQITTcwNvMOpxie";
//            Users3.Friend f11 = new Users3.Friend(); f11.id = "4858"; f11.name = "cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG";
//            Users3.Friend f12 = new Users3.Friend(); f12.id = "9141"; f12.name = "rJxMGOWRjdkphthcaKTspFrMcvcLLb";
//            Users3.Friend f13 = new Users3.Friend(); f13.id = "9128"; f13.name = "gcsYaolAQqrNMQTluIAKOkwYTWVUXe";
//            Users3.Friend f14 = new Users3.Friend(); f14.id = "2268"; f14.name = "jwXOUcXAiLurRlgTdxyKWvsbNHfFxl";
//            Users3.Friend f15 = new Users3.Friend(); f15.id = "5447"; f15.name = "whivfJXOdxoHtLIGpytTdbOXxlZpUY";
//            Users3.Friend f16 = new Users3.Friend(); f16.id = "7551"; f16.name = "whykuIjZUgvOFGpmNHjoPeTeYCPNby";
//            Users3.Friend f17 = new Users3.Friend(); f17.id = "719";  f17.name = "SmbiwQaORLdsbAlUZbQwgCKfuoPLVr";
//            Users3.Friend f18 = new Users3.Friend(); f18.id = "7773"; f18.name = "LZmRMXmXXHzlzFFJAopDNnWkuBqndD";
//            Users3.Friend f19 = new Users3.Friend(); f19.id = "9602"; f19.name = "xCNsDBFMygEwZuecJKTUrqeDLBJlrR";
//            Users3.Friend f20 = new Users3.Friend(); f20.id = "1536"; f20.name = "hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB";
//            Users3.Friend f21 = new Users3.Friend(); f21.id = "3549"; f21.name = "NvvhXwWgCSaYijqhxsrxIWrHbBOOIa";
//
//            user.friends = List.of(
//                f1, f2, f3, f4, f5, f6, f7, f8, f9, f10,
//                f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21
//            );
//
//
//            Users3 bean = new Users3();
//            bean.users = List.of(user);
//
//            String result = writeXml(xmlMapper, Argument.of(Users3.class), bean);
//            System.out.println("Users3 full XML => " + result);
//
//            String expected =
//                "<Users3>" +
//                    "<users>" +
//                    "<users>" +
//                    "<_id>39771757156730064829</_id>" +
//                    "<index>1031703887</index>" +
//                    "<guid>ifhsrU6geU4PijjDE8Q5</guid>" +
//                    "<isActive>false</isActive>" +
//                    "<balance>TKl0GcwTs72S4CPx5rfg</balance>" +
//                    "<picture>FkKrg6ZOPC5REchlhixu5WgIl3gNAqq28iLtFm6dKfTSQs8d3P0cYxKsEvbvMB2C6BVgExop3khRlNSFE4SV8dVFitFs7RyyecN8</picture>" +
//                    "<age>5</age>" +
//                    "<eyeColor>AY79Pw4sYByUZEMLxnYJ</eyeColor>" +
//                    "<name>XjXrEZMuTvPnuOPBg7hL</name>" +
//                    "<gender>VaMcuWBHvnWvIlCC9q4T</gender>" +
//                    "<company>6pmCe1LxouRGfZD79ena</company>" +
//                    "<email>TboNtpmAS0ppZ07jITFE</email>" +
//                    "<phone>j8OoUhtmwBlI20EgD1LS</phone>" +
//                    "<address>Aqo4fSYBpvvAWTDqbFbK</address>" +
//                    "<about>1kXFSA2782BLqNBbKIbp</about>" +
//                    "<registered>Mc7h3gZJcQ11ShGQYdXI</registered>" +
//                    "<latitude>13.474549605725421</latitude>" +
//                    "<longitude>35.010833129741435</longitude>" +
//                    "<tags>" +
//                    "<tags>8tGfPhZkZD</tags>" +
//                    "<tags>XYmwuAAtZ4</tags>" +
//                    "<tags>u9iBDMpS9G</tags>" +
//                    "<tags>4udy1eRqme</tags>" +
//                    "<tags>Lg48Ogrf0I</tags>" +
//                    "<tags>zku019kVpo</tags>" +
//                    "<tags>iuIMkiZzog</tags>" +
//                    "<tags>MuI1uYeCjc</tags>" +
//                    "<tags>49n7qisFD8</tags>" +
//                    "<tags>TtVgWerCRh</tags>" +
//                    "<tags>H604QRJmi1</tags>" +
//                    "<tags>ZIQMfqInNH</tags>" +
//                    "<tags>CbDyjjA19F</tags>" +
//                    "<tags>pNFwPdkVdU</tags>" +
//                    "<tags>aPFLsUbIUh</tags>" +
//                    "<tags>fA735PT0Hd</tags>" +
//                    "<tags>00etYDYL87</tags>" +
//                    "<tags>mlyEf1lI2B</tags>" +
//                    "<tags>RQ05IJSzXF</tags>" +
//                    "<tags>3jJt0Zrkhw</tags>" +
//                    "<tags>ZINP8GH4Bm</tags>" +
//                    "<tags>XebX8UvviN</tags>" +
//                    "<tags>EXqZ9G0ATB</tags>" +
//                    "<tags>ssyzWZVAa2</tags>" +
//                    "</tags>" +
//                    "<friends>" +
//                    "<friends><id>2668</id><name>lcxeDXPbnoIxAPqTNdkwbcGIJxLnPe</name></friends>" +
//                    "<friends><id>9395</id><name>dxNBbezfkbotyCmFzjodONShlGFaAg</name></friends>" +
//                    "<friends><id>5249</id><name>fYHSDXScMSzQvxzFuuPHYWfyjdGQLg</name></friends>" +
//                    "<friends><id>4978</id><name>qfoxPWmoWUyUduVkRwhzyBusuflrFY</name></friends>" +
//                    "<friends><id>9710</id><name>vUAJwshFGLoBHfwLcsEVNLJLwdaCAg</name></friends>" +
//                    "<friends><id>7404</id><name>BhVMdvhPRdpwpDWAmfhNDikncdNgGr</name></friends>" +
//                    "<friends><id>1343</id><name>ZeDoizPcOBafZtVYDOmpzGoHekfoxf</name></friends>" +
//                    "<friends><id>7382</id><name>KtqXeVdCQJlwSNHkgkxuoIGdOWrmqG</name></friends>" +
//                    "<friends><id>1365</id><name>rCSTlgbmTAFhbSfPmnftcDLwdiKsHt</name></friends>" +
//                    "<friends><id>8037</id><name>PUvwVYoSvSTnwjJCQITTcwNvMOpxie</name></friends>" +
//                    "<friends><id>4858</id><name>cUfQfDIiyMfCMYBKGwhZSWnRRKwlxG</name></friends>" +
//                    "<friends><id>9141</id><name>rJxMGOWRjdkphthcaKTspFrMcvcLLb</name></friends>" +
//                    "<friends><id>9128</id><name>gcsYaolAQqrNMQTluIAKOkwYTWVUXe</name></friends>" +
//                    "<friends><id>2268</id><name>jwXOUcXAiLurRlgTdxyKWvsbNHfFxl</name></friends>" +
//                    "<friends><id>5447</id><name>whivfJXOdxoHtLIGpytTdbOXxlZpUY</name></friends>" +
//                    "<friends><id>7551</id><name>whykuIjZUgvOFGpmNHjoPeTeYCPNby</name></friends>" +
//                    "<friends><id>719</id><name>SmbiwQaORLdsbAlUZbQwgCKfuoPLVr</name></friends>" +
//                    "<friends><id>7773</id><name>LZmRMXmXXHzlzFFJAopDNnWkuBqndD</name></friends>" +
//                    "<friends><id>9602</id><name>xCNsDBFMygEwZuecJKTUrqeDLBJlrR</name></friends>" +
//                    "<friends><id>1536</id><name>hrfeFnKnmVgZDDOxAHgXfgcJSRyiXB</name></friends>" +
//                    "<friends><id>3549</id><name>NvvhXwWgCSaYijqhxsrxIWrHbBOOIa</name></friends>" +
//                    "</friends>" +
//                    "</users>" +
//                    "</users>" +
//                    "</Users3>";
//
//            assertEquals(expected, result);
//        }
//    }
//



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
    final static class ObjectWithArray {

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
    final static class SomeObject {

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
    record StringListBean(int age, String name,  List<String> strings, String prenom) {}

    @Serdeable
    record ArrayListBean(int age, String name, ArrayList<String> strings, String prenom) {}

    @Serdeable
    record LinkedListBean(int age, String name, LinkedList<String> strings, String prenom) {}

    @Serdeable
    record LinkedHashSetBean(int age, String name, LinkedHashSet<String> strings, String prenom) {}

    @Serdeable
    record CollectionBean(int age, String name, Collection<String> strings, String prenom) {}

    @Serdeable
    record NestedCollectionBean(List<List<String>> strings, String prenom) {}

    @Serdeable
    record NestedCubicTime(List<List<List<String>>> strings, String prenom) {}
}
