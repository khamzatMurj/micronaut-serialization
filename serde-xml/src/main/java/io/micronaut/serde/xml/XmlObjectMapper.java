package io.micronaut.serde.xml;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonStreamConfig;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.LimitingStream;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.SerdeIntrospections;
import io.micronaut.serde.SerdeRegistry;
import io.micronaut.serde.Serializer;
import io.micronaut.serde.config.SerdeConfiguration;
import io.micronaut.serde.support.util.JsonNodeDecoder;
import io.micronaut.serde.support.util.JsonNodeEncoder;
import io.micronaut.serde.xml.annotation.XmlRootName;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.dataformat.xml.XmlFactory;
import tools.jackson.dataformat.xml.deser.FromXmlParser;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * XML Mapper
 *
 * @author Mousrij Hamza
 */
@Singleton
@Named("xml")
@Internal
public class XmlObjectMapper implements ObjectMapper {

    private final SerdeRegistry registry;
    private final XmlFactory xmlFactory;
    private final SerdeIntrospections introspections;
    @Nullable
    private final SerdeConfiguration serdeConfiguration;
    private final Serializer.EncoderContext encoderContext;
    private final Deserializer.DecoderContext decoderContext;
    @Nullable
    private final String defaultRootName;

    public XmlObjectMapper(SerdeRegistry registry,
            SerdeIntrospections introspections,
            SerdeConfiguration serdeConfiguration,
            @Nullable XmlSerdeConfiguration xmlConfiguration) {
        this.registry = registry;
        this.introspections = introspections;
        this.serdeConfiguration = serdeConfiguration;
        this.xmlFactory = XmlFactory.builder().build();
        this.encoderContext = registry.newEncoderContext(null);
        this.decoderContext = registry.newDecoderContext(null);
        this.defaultRootName = xmlConfiguration != null ? xmlConfiguration.getDefaultRootName() : null;
    }

    @Override
    public @NonNull SerdeRegistry getSerdeRegistry() {
        return this.registry;
    }

    private LimitingStream.@NonNull RemainingLimits limits() {
        return serdeConfiguration == null
                ? LimitingStream.DEFAULT_LIMITS
                : LimitingStream.limitsFromConfiguration(serdeConfiguration);
    }

    // --- Write ---

    @Override
    public <T> void writeValue(OutputStream outputStream, Argument<T> type, @Nullable T object) throws IOException {
        try (ToXmlGenerator generator = createGenerator(outputStream)) {
            if (object == null) {
                generator.writeNull();
            } else {
                generator.setNextName(new QName(XMLConstants.NULL_NS_URI, resolveRootName(type)));

                XmlGeneratorEncoder encoder = new XmlGeneratorEncoder(generator, limits());
                serialize(encoder, object, type);
            }
            generator.flush();
        }
    }

    @Override
    public <T> byte[] writeValueAsBytes(Argument<T> type, @Nullable T object) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeValue(output, type, object);
        return output.toByteArray();
    }

    @Override
    public void writeValue(OutputStream outputStream, @Nullable Object object) throws IOException {
        try (ToXmlGenerator generator = createGenerator(outputStream)) {
            if (object == null) {
                generator.writeNull();
            } else {
                Argument<?> type = Argument.of(object.getClass());
                // null namespace
                generator.setNextName(new QName(XMLConstants.NULL_NS_URI, resolveRootName(type)));

                XmlGeneratorEncoder encoder = new XmlGeneratorEncoder(generator, limits());
                serialize(encoder, object, type);
            }
            generator.flush();
        }
    }

    @Override
    public byte[] writeValueAsBytes(@Nullable Object object) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeValue(output, object);
        return output.toByteArray();
    }

    @Override
    public <T> JsonNode writeValueToTree(Argument<T> type, @Nullable T value) throws IOException {
        JsonNodeEncoder encoder = JsonNodeEncoder.create(limits());
        serialize(encoder, value, type);
        return encoder.getCompletedValue();
    }

    @Override
    public JsonNode writeValueToTree(@Nullable Object value) throws IOException {
        JsonNodeEncoder encoder = JsonNodeEncoder.create(limits());
        serialize(encoder, value, Argument.of(value.getClass()));
        return encoder.getCompletedValue();
    }

    // --- Read ---

    @Override
    public <T> T readValue(InputStream inputStream, Argument<T> type) throws IOException {
        return readValue(toByteBuffer(inputStream), type);
    }

    @Override
    public <T> T readValue(byte[] byteArray, Argument<T> type) throws IOException {
        try (FromXmlParser parser = createParser(byteArray)) {
            if (parser.currentToken() == null) {
                parser.nextToken();
            }

            return decoderContext.findDeserializer(type)
                    .createSpecific(decoderContext, type)
                    .deserialize(new XmlParserDecoder(parser, limits()), decoderContext, type);
        }

    }

    @Override
    public <T> T readValue(String string, Argument<T> type) throws IOException {
        return readValue(string.getBytes(StandardCharsets.UTF_8), type);
    }

    @Override
    public <T> T readValueFromTree(JsonNode tree, Argument<T> type) throws IOException {
        Deserializer<? extends T> deserializer = decoderContext.findDeserializer(type)
                .createSpecific(decoderContext, type);
        return deserializer.deserialize(JsonNodeDecoder.create(tree, limits()), decoderContext, type);
    }

    // Configuring the stream

    @Override
    public JsonStreamConfig getStreamConfig() {
        return JsonStreamConfig.DEFAULT;
    }

    // ---- Internal helpers for the XmlFactory.createGenerator writer method
    // builder and the parser reader ----

    private ToXmlGenerator createGenerator(OutputStream outputStream) {
        return (ToXmlGenerator) xmlFactory.createGenerator(
                tools.jackson.core.ObjectWriteContext.empty(),
                outputStream);
    }

    private FromXmlParser createParser(byte[] byteArray) {
        return (FromXmlParser) xmlFactory.createParser(
                tools.jackson.core.ObjectReadContext.empty(),
                byteArray);
    }

    private String resolveRootName(Argument<?> type) {
        String annotationRootName = null;

        try {
            BeanIntrospection<?> introspection = introspections.getSerializableIntrospection(type);
            annotationRootName = introspection.stringValue(XmlRootName.class).orElse(null);
        } catch (Exception ignored) {
            annotationRootName = null;
        }
        if (annotationRootName != null && !annotationRootName.isBlank()) {
            return annotationRootName;
        }
        if (defaultRootName != null) {
            return defaultRootName;
        }
        String name = type.getSimpleName();
        return (name == null || name.isEmpty()) ? "root" : name;
    }

    private void serialize(Encoder encoder, Object object, Argument type) throws IOException {
        Serializer<Object> serializer = encoderContext.findSerializer(type).createSpecific(encoderContext, type);
        serializer.serialize(encoder, encoderContext, type, object);
    }

    private byte[] toByteBuffer(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[512];
        int nbByteRead /* = 0 */;
        while ((nbByteRead = inputStream.read(byteBuffer)) != -1) {
            // appends buffer
            baos.write(byteBuffer, 0, nbByteRead);
        }
        return baos.toByteArray();
    }
}
