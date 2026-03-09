package io.micronaut.serde.xml;

import io.micronaut.serde.exceptions.SerdeException;
import io.micronaut.serde.support.AbstractDecoderPerStructureStreamDecoder;
import io.micronaut.serde.support.AbstractStreamDecoder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonToken;
import tools.jackson.dataformat.xml.deser.FromXmlParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * XML implementation of {@link io.micronaut.serde.Decoder}.
 *
 * @author Mousrij Hamza
 */
public final class XmlParserDecoder extends AbstractDecoderPerStructureStreamDecoder {

    private final FromXmlParser parser;
    private TokenType currentTokenType;

    public XmlParserDecoder(FromXmlParser parser, @NonNull RemainingLimits remainingLimits) {
        super(remainingLimits);
        this.parser = parser;
        this.currentTokenType = mapToken(parser.currentToken());
    }

    public XmlParserDecoder(@NonNull XmlParserDecoder parent, @NonNull RemainingLimits remainingLimits) {
        super(parent, remainingLimits);
        this.parser = parent.parser;
        this.currentTokenType = parent.currentTokenType;
    }

    private static @Nullable TokenType mapToken(@Nullable JsonToken token) {
        if (token == null) {
            return null;
        }
        return switch (token) {
            case START_OBJECT -> TokenType.START_OBJECT;
            case END_OBJECT -> TokenType.END_OBJECT;
            case START_ARRAY -> TokenType.START_ARRAY;
            case END_ARRAY -> TokenType.END_ARRAY;
            case PROPERTY_NAME -> TokenType.KEY;
            case VALUE_STRING -> TokenType.STRING;
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> TokenType.NUMBER;
            case VALUE_TRUE, VALUE_FALSE -> TokenType.BOOLEAN;
            case VALUE_NULL -> TokenType.NULL;
            default -> TokenType.OTHER;
        };
    }

    @Override
    protected void backFromChild(AbstractStreamDecoder child) throws IOException {
        XmlParserDecoder xmlChild = (XmlParserDecoder) child;
        this.currentTokenType = xmlChild.currentTokenType;
        super.backFromChild(child);
    }

    @Override
    protected AbstractStreamDecoder createChildDecoder() throws SerdeException {
        return new XmlParserDecoder(this, childLimits());
    }


    @Override
    protected @Nullable TokenType currentToken() {
        return currentTokenType;
    }

    @Override
    protected void nextToken() throws IOException {
        JsonToken nextToken = parser.nextToken();
        currentTokenType = mapToken(nextToken);
    }

    @Override
    protected String getCurrentKey() throws IOException {
        return parser.currentName();
    }

    @Override
    protected String getString() throws IOException {
        return parser.getString();
    }

    @Override
    protected boolean getBoolean() throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_TRUE) {
            return true;
        } else if (token == JsonToken.VALUE_FALSE) {
            return false;
        }
        return Boolean.parseBoolean(parser.getString());
    }

    @Override
    protected long getLong() throws IOException {
        return parser.getLongValue();
    }

    @Override
    protected double getDouble() throws IOException {
        return parser.getDoubleValue();
    }

    @Override
    protected BigInteger getBigInteger() throws IOException {
        return parser.getBigIntegerValue();
    }

    @Override
    protected BigDecimal getBigDecimal() throws IOException {
        return parser.getDecimalValue();
    }

    @Override
    protected Number getBestNumber() throws IOException {
        return parser.getNumberValue();
    }

    @Override
    protected void skipChildren() throws IOException {
        parser.skipChildren();
    }

    @Override
    public @NonNull IOException createDeserializationException(@NonNull String message, @Nullable Object invalidValue) {
        return new SerdeException(message + " \n at ");
    }

    @Override
    protected String coerceScalarToString(TokenType currentToken) throws IOException {
        return parser.getString();
    }


}
