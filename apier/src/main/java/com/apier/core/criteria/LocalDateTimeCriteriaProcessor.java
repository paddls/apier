package com.apier.core.criteria;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.time.LocalDateTime;

public class LocalDateTimeCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            final Class<?> dateClass = Class.forName(field.getTypeName());
            return LocalDateTime.class.isAssignableFrom(dateClass);
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        final TypeName fieldType = ClassName.get(LocalDateTimeCriteria.class);
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
