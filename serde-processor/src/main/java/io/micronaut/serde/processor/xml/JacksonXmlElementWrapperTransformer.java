package io.micronaut.serde.processor.xml;


import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.serde.config.annotation.SerdeConfig;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class JacksonXmlElementWrapperTransformer implements NamedAnnotationMapper {

    @Override
    public @NonNull String getName() {
        return "tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper";
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
        boolean useWrapping = annotation.booleanValue("useWrapping").orElse(true);
        if (!useWrapping) {
            return Collections.emptyList();
        }
        AnnotationValueBuilder<SerdeConfig> builder = AnnotationValue.builder(SerdeConfig.class);
        annotation.stringValue("localName")
            .ifPresent(s -> builder.member(SerdeConfig.SerSubtyped.DISCRIMINATOR_TYPE, s));


        return Collections.singletonList(builder.build());

    }
}
