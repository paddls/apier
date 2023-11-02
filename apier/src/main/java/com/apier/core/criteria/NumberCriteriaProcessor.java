package com.apier.core.criteria;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class NumberCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            return Number.class.isAssignableFrom(Class.forName(field.getTypeName()));
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        final TypeName fieldType = ParameterizedTypeName.get(
            ClassName.get(NumberCriteria.class),
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
