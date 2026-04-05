/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.serde.xml;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.LimitingStream;
import io.micronaut.serde.config.annotation.SerdeConfig;
import org.jspecify.annotations.NonNull;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

/**
 * XML implementation of the {@link Encoder}.
 *
 * @author Mousrij Hamza
 */
public final class XmlGeneratorEncoder extends LimitingStream implements Encoder {

    private final ToXmlGenerator generator;
    private final XmlGeneratorEncoder parent;
    private final boolean isArray;
    @Nullable
    private final XmlSerdeConfiguration xmlConfiguration;

    private String currentKey;
    private int currentIndex;


    public XmlGeneratorEncoder(ToXmlGenerator generator, @NonNull RemainingLimits remainingLimits, @Nullable XmlSerdeConfiguration xmlConfiguration) {
        super(remainingLimits);
        this.generator = generator;
        this.parent = null;
        this.isArray = false;
        this.xmlConfiguration = xmlConfiguration;
    }

    public XmlGeneratorEncoder(XmlGeneratorEncoder parent, @NonNull RemainingLimits remainingLimits, boolean isArray) {
        super(remainingLimits);
        this.generator = parent.generator;
        this.parent = parent;
        this.isArray = isArray;
        this.xmlConfiguration = parent.xmlConfiguration;

    }

    private void postEncodeValue() {
        currentIndex++;
    }


    @Override
    public @NonNull Encoder encodeArray(@NonNull Argument<?> type) throws IOException {
        generator.writeStartArray();
        System.out.println("Encoding array start");
        System.out.println(type.getAnnotationMetadata().getAnnotationNames());
        System.out.println(type.getAnnotationMetadata().stringValue(SerdeConfig.class, SerdeConfig.XML_FIELD_WRAPPER).orElse(null));
        return new XmlGeneratorEncoder(this, childLimits(), true);
    }




    @Override
    public void startWrappedValue(Argument<?> type, String wrapper) throws IOException {
        if (type != null){
            if (Map.class.isAssignableFrom(type.getType())) {
                return;
            }
            if (wrapper != null) {
                System.out.println("encoder wrapper ; " + wrapper);
                generator.startWrappedValue(new QName(type.getName()),
                    new QName(
                        XMLConstants.NULL_NS_URI,
                        wrapper,
                        XMLConstants.DEFAULT_NS_PREFIX));
            }
        } else {
            generator.startWrappedValue(null,
                new QName(
                    XMLConstants.NULL_NS_URI,
                    wrapper,
                    XMLConstants.NULL_NS_URI));
        }
    }

    @Override
    public void finishWrappedValue(Argument<?> type, @Nullable String wrapper) throws IOException {
        if  (type != null){
            if (Map.class.isAssignableFrom(type.getType())) {
                return;
            }
            if (wrapper != null) {
                generator.finishWrappedValue(new QName(type.getName()), new QName(wrapper));
            }
        }  else {
            generator.finishWrappedValue(null,
                new QName(
                    XMLConstants.NULL_NS_URI,
                    wrapper,
                    XMLConstants.NULL_NS_URI));
        }

    }


    @Override
    public @NonNull Encoder encodeObject(@NonNull Argument<?> type) throws IOException {
        generator.writeStartObject();
        XmlGeneratorEncoder child = new XmlGeneratorEncoder(this, childLimits(), false);
        return child;
    }

    @Override
    public void finishStructure() throws IOException {
        Optional.ofNullable(parent).orElseThrow(
            () -> new IllegalStateException("Not in a structure"));
        try {
            if (isArray) {
                generator.writeEndArray();
                //generator.finishWrappedValue(new QName("AA"), new QName("item"));
            } else {
                generator.writeEndObject();
            }
        } catch (Exception e) {
            throw new IOException("Failed to finish structure", e);
        }
        parent.postEncodeValue();
    }

    @Override
    public void encodeKey(@NonNull String key) throws IOException {
        this.currentKey = key;
        generator.writeName(key);
    }

    // XmlGeneratorEncoder.java — encodeString
    @Override
    public void encodeString(@NonNull String value) throws IOException {
        generator.writeString(value);
        postEncodeValue();
    }

    @Override
    public void close() throws IOException {
        Encoder.super.close();
    }

    @Override
    public void encodeBoolean(boolean value) throws IOException {
        generator.writeBoolean(value);
        postEncodeValue();
    }

    @Override
    public void encodeByte(byte value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeShort(short value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeChar(char value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeInt(int value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeLong(long value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeFloat(float value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeDouble(double value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeBigInteger(@NonNull BigInteger value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeBigDecimal(@NonNull BigDecimal value) throws IOException {
        generator.writeNumber(value);
        postEncodeValue();
    }

    @Override
    public void encodeNull() throws IOException {
        boolean writeNullsAsXml = xmlConfiguration.getXmlWriteFeatures().get("write-nulls-as-xsi-nil");
        //generator.configure(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL, writeNullsAsXml);
        generator.writeNull();
        postEncodeValue();
    }


}
