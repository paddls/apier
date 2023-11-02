package com.apier.core.criteria;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.time.ZonedDateTime;

public class ZonedDateTimeCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            final Class<?> dateClass = Class.forName(field.getTypeName());
            return ZonedDateTime.class.isAssignableFrom(dateClass);
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        final TypeName fieldType = ClassName.get(ZonedDateTimeCriteria.class);
        return FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE).build();
    }

    @Override
    public CodeBlock getPredicate(final CriteriaFieldBuilder field) {
        return CodeBlock.builder().addStatement("if($L != null) { predicate.and($L.toPredicate($L.$L)); }", field.getName(), field.getName(), field.getCriteriaClassBuilder().getQClassAlias(), field.getName()).build();
    }
}
