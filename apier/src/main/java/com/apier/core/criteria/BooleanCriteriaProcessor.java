package com.apier.core.criteria;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Modifier;

public class BooleanCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            return Boolean.class.isAssignableFrom(Class.forName(field.getTypeName()));
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        return FieldSpec.builder(ClassName.get(BooleanCriteria.class), field.getName(), Modifier.PRIVATE).build();
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
