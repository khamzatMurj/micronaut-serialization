package io.micronaut.serde.xml;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.LimitingStream;
import org.jspecify.annotations.NonNull;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    private String currentKey;
    private int currentIndex;

    public XmlGeneratorEncoder(ToXmlGenerator generator, @NonNull RemainingLimits remainingLimits) {
        super(remainingLimits);
        this.generator = generator;
        this.parent = null;
        this.isArray = false;
    }

    public XmlGeneratorEncoder(XmlGeneratorEncoder parent, @NonNull RemainingLimits remainingLimits, boolean isArray) {
        super(remainingLimits);
        this.generator = parent.generator;
        this.parent = parent;
        this.isArray = isArray;
    }

    private void postEncodeValue() {
        currentIndex++;
    }

    @Override
    public @NonNull Encoder encodeArray(@NonNull Argument<?> type) throws IOException {
        generator.writeStartArray();
        return new XmlGeneratorEncoder(this, childLimits(), true);
    }

    @Override
    public @NonNull Encoder encodeObject(@NonNull Argument<?> type) throws IOException {
        generator.writeStartObject();
        return new XmlGeneratorEncoder(this, childLimits(), false);
    }

    @Override
    public void finishStructure() throws IOException {
        Optional.ofNullable(parent).orElseThrow(
            () -> {
                throw new IllegalStateException("Not in a structure");
            });
        try {
            if (isArray) {
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

    @Override
    public void encodeString(@NonNull String value) throws IOException {
        generator.writeString(value);
        postEncodeValue();
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
        generator.writeNull();
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
}
