package com.apier.core.criteria.querydsl.processor;

import com.apier.core.criteria.CriteriaFieldBuilder;
import com.apier.core.criteria.CriteriaProcessor;
import com.apier.core.criteria.querydsl.criteria.QNumberCriteria;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class QNumberCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            return field.getCriteriaClassBuilder().resourceHasAnnotation("jakarta.persistence.Entity") && Number.class.isAssignableFrom(Class.forName(field.getTypeName()));
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        final TypeName fieldType = ParameterizedTypeName.get(
                ClassName.get(QNumberCriteria.class),
            ClassName.bestGuess(field.getTypeName())
        );
        return FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE).build();
    }

    @Override
    public CodeBlock getPredicate(final CriteriaFieldBuilder field) {
        return CodeBlock
            .builder()
            .addStatement(
                "if($L != null) { predicate.and($L.toPredicate($L.$L)); }",
                field.getName(),
                field.getName(),
                field.getCriteriaClassBuilder().getQClassAlias(),
                field.getName()
            )
            .build();
    }
}
