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
import org.jspecify.annotations.NonNull;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
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
    private final Deque<ArrayContext> arrayContext;

    public XmlGeneratorEncoder(ToXmlGenerator generator, @NonNull RemainingLimits remainingLimits, @Nullable XmlSerdeConfiguration xmlConfiguration) {
        super(remainingLimits);
        this.generator = generator;
        this.parent = null;
        this.isArray = false;
        this.xmlConfiguration = xmlConfiguration;
        this.arrayContext = new ArrayDeque<>();
    }

    public XmlGeneratorEncoder(XmlGeneratorEncoder parent, @NonNull RemainingLimits remainingLimits, boolean isArray, Deque<ArrayContext> arrayContext) {
        super(remainingLimits);
        this.generator = parent.generator;
        this.parent = parent;
        this.isArray = isArray;
        this.xmlConfiguration = parent.xmlConfiguration;
        this.arrayContext = arrayContext;
    }

    private void postEncodeValue() {
        currentIndex++;
    }


    @Override
    public @NonNull Encoder encodeArray(@NonNull Argument<?> type) throws IOException {
        ArrayContext parentCtx = this.arrayContext.peek();

        // Resolve logical XML name.
        // wrapperType key tag set as Type instead of name
        String logicalName;
        if (parentCtx != null) {

            Argument<?> typeParameter = type.getTypeParameters()[0];
            logicalName = xmlLocalName(typeParameter);
        } else if (this.currentKey != null) {
            logicalName = this.currentKey;
        } else {
            //logicalName = type.getTypeName(); // type : "List<List<SomeObject E> E> vals" ==> vals
            logicalName = xmlLocalName(type);
        }
        QName qname = new QName(XMLConstants.NULL_NS_URI, logicalName);

        if (parentCtx != null) {
            // Opening a nested array: open wrapper element
            QName wn = (currentIndex == 0) ? parentCtx.itemName() : null;
            generator.startWrappedValue(wn, parentCtx.itemName());
        }

        this.arrayContext.push(new ArrayContext(qname, qname, type));
        generator.writeStartArray();
        return new XmlGeneratorEncoder(this, childLimits(), true, this.arrayContext);
    }

    @Override
    public @NonNull Encoder encodeObject(@NonNull Argument<?> type) throws IOException {
        boolean insideArray = !this.arrayContext.isEmpty();
        if (insideArray) {
            ArrayContext ctx = this.arrayContext.peek();
            QName wrapperName = (currentIndex == 0) ? ctx.wrapperName() : null;
            generator.startWrappedValue(wrapperName, ctx.itemName());
        }
        generator.writeStartObject();
        Deque<ArrayContext> contexts = new ArrayDeque<>();
        XmlGeneratorEncoder child = new XmlGeneratorEncoder(this, childLimits(), false, contexts);
        return child;
    }

    @Override
    public void finishStructure() throws IOException {
        Optional.ofNullable(parent).orElseThrow(
            () -> new IllegalStateException("Not in a structure"));
        try {
            if (isArray) {
                ArrayContext ctx = this.arrayContext.peek();
                if (ctx != null) {
                    // Close the wrapper element that startWrappedValue opened
                    generator.finishWrappedValue(ctx.wrapperName(), ctx.itemName());
                    this.arrayContext.pop(); // pop only this level
                }
                generator.writeEndArray();
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
        ArrayContext peeked = this.arrayContext.peek();

        if (peeked != null && peeked.itemName() != null) {

            // Pass null for subsequent items so Jackson reuses the open wrapper.
            QName wrapperName = (currentIndex == 0) ? peeked.itemName() : null;
            QName wrappedName = peeked.itemName();
            // Convention wrapped name same as wrapper name
            generator.startWrappedValue(
                wrapperName,
                wrappedName
            );
        }
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
        generator.configure(XmlWriteFeature.WRITE_NULLS_AS_XSI_NIL, writeNullsAsXml);
        ArrayContext ctx = this.arrayContext.peek();
        if (ctx != null) {
            // Inside an array: open the wrapper element for this null item so that
            // finishStructure's finishWrappedValue has a matching open element.
            QName wn = (currentIndex == 0) ? ctx.itemName() : null;
            generator.startWrappedValue(wn, ctx.itemName());
        }
        generator.writeNull();
        if (ctx != null) {
            generator.finishWrappedValue(null, ctx.itemName());
        }
        postEncodeValue();
    }

    @Override
    public @NonNull String currentPath() {
        StringBuilder builder = new StringBuilder();
        XmlGeneratorEncoder enc = this;
        while (enc != null) {
            if (enc != this) {
                builder.insert(0, "->");
            }
            if (enc.currentKey == null) {
                if (enc.parent != null) {
                    builder.insert(0, enc.currentIndex);
                }
            } else {
                builder.insert(0, enc.currentKey);
            }
            enc = enc.parent;
        }
        return builder.toString();
    }

    public ToXmlGenerator getGenerator() {
        return generator;
    }

    public void setNextIsAttribute(boolean isAttribute) {
        generator.setNextIsAttribute(isAttribute);
    }

    public void setNextIsCData(boolean isCData) {
        generator.setNextIsCData(isCData);
    }

    public void setNextName(QName name) {
        generator.setNextName(name);
    }

    private record ArrayContext(
        QName wrapperName,    // passed as first arg to startWrappedValue / finishWrappedValue
        QName itemName,       // passed as second arg (per-element tag)
        Argument<?> elementType
    ) {

    }

    // helper method to set the Xml tag local name for wrapperType
    private static String xmlLocalName(Argument<?> argument) throws IOException {
        Class<?> type = argument.getType();
        String simpleName = type.getSimpleName();
        if (simpleName != null && !simpleName.isEmpty()) {
            return simpleName;
        }
        throw new IOException(" Type unresolvable: " + argument);

    }


}
