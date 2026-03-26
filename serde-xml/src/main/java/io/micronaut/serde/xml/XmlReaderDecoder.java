package io.micronaut.serde.xml;

import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.LimitingStream;
import io.micronaut.serde.exceptions.SerdeException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonToken;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class XmlReaderDecoder extends LimitingStream implements Decoder {

    FromXmlParser parser;
    Boolean isEndOfObject = false;

    public XmlReaderDecoder(FromXmlParser parser, @NonNull RemainingLimits remainingLimits) {
        super(remainingLimits);
        this.parser = parser;
    }
    @Override
    public @NonNull Decoder decodeObject(@NonNull Argument<?> type) throws IOException {
        JsonToken token = parser.currentToken();
        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected START_OBJECT but got " + token);
        }
        var t =parser.nextToken();
        return this;
    }
    @Override
    public @NonNull Decoder decodeArray(Argument<?> type) throws IOException {
        parser.isExpectedStartArrayToken();
        var token = parser.currentToken();
        var t = parser.nextToken();
        return this;
    }

    @Override
    public boolean hasNextArrayValue() throws IOException {

        // still more element return true
        var token = parser.currentToken();
        // Todo:
        if (token != JsonToken.END_ARRAY) {
            return true;
        }

        return false;
    }

    @Override
    public void finishStructure() throws IOException {
        Decoder.super.finishStructure();
        var t2 = parser.nextToken();
    }

    @Override
    public void finishStructure(boolean consumeLeftElements) throws IOException {

    }

    @Override
    public @Nullable String decodeKey() throws IOException {
        if (isEndOfObject()) {
            return null;
        }
        String key = parser.currentName();
        var token = parser.nextToken();
        return key;
    }

    @Override
    public @NonNull String decodeString() throws IOException {
        if (parser.currentToken() == JsonToken.END_ARRAY) {
            throw new SerdeException("Expected END_ARRAY but got " + parser.currentToken());
        }
        String string = parser.getString();
        var t = parser.nextToken();
        return string;
    }

    @Override
    public boolean decodeNull() throws IOException {
        //Todo : check currentToken and make if it true or not
        return false;
    }


    @Override
    public boolean decodeBoolean() throws IOException {
        return false;
    }

    @Override
    public byte decodeByte() throws IOException {
        return 0;
    }

    @Override
    public short decodeShort() throws IOException {
        return 0;
    }

    @Override
    public char decodeChar() throws IOException {
        return 0;
    }

    @Override
    public int decodeInt() throws IOException {
        var intValue = Integer.parseInt(parser.getString());
        parser.nextToken();
        return intValue;
    }

    @Override
    public long decodeLong() throws IOException {
        return 0;
    }

    @Override
    public float decodeFloat() throws IOException {
        return 0;
    }

    @Override
    public double decodeDouble() throws IOException {
        return 0;
    }

    @Override
    public @NonNull BigInteger decodeBigInteger() throws IOException {
        return null;
    }

    @Override
    public @NonNull BigDecimal decodeBigDecimal() throws IOException {
        return null;
    }

    @Override
    public @Nullable Object decodeArbitrary() throws IOException {
        return null;
    }

    @Override
    public @NonNull JsonNode decodeNode() throws IOException {
        return null;
    }

    @Override
    public Decoder decodeBuffer() throws IOException {
        return null;
    }

    @Override
    public void skipValue() throws IOException {

    }

    @Override
    public @NonNull IOException createDeserializationException(@NonNull String message, @Nullable Object invalidValue) {
        return new SerdeException(message + " \n at " + parser.currentLocation());
    }

    private void next() {
        JsonToken nextToken = parser.nextToken();
        isEndOfObject = nextToken == JsonToken.END_OBJECT;

    }

    private boolean isEndOfObject() {
        return isEndOfObject || parser.currentToken() == JsonToken.END_OBJECT;
    }
}
