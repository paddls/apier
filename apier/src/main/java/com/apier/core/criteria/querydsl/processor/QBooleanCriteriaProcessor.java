package com.apier.core.criteria.querydsl.processor;

import com.apier.core.criteria.CriteriaFieldBuilder;
import com.apier.core.criteria.CriteriaProcessor;
import com.apier.core.criteria.querydsl.criteria.QBooleanCriteria;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Modifier;

public class QBooleanCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        try {
            return field.getCriteriaClassBuilder().resourceHasAnnotation("jakarta.persistence.Entity") && Boolean.class.isAssignableFrom(Class.forName(field.getTypeName()));
        } catch (final Exception exception) {
            return false;
        }
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        return FieldSpec.builder(ClassName.get(QBooleanCriteria.class), field.getName(), Modifier.PRIVATE).build();
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
