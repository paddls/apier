package com.apier.core.criteria;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Modifier;

public class DeepCriteriaProcessor implements CriteriaProcessor {

    @Override
    public boolean canApply(final CriteriaFieldBuilder field) {
        return field.isDeep();
    }

    @Override
    public FieldSpec getField(final CriteriaFieldBuilder field) {
        return FieldSpec
            .builder(ClassName.bestGuess(field.getCriteriaTypeName()), field.getName(), Modifier.PRIVATE)
            .build();
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
