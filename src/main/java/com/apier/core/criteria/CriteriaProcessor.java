package com.apier.core.criteria;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;

public interface CriteriaProcessor {
    boolean canApply(final CriteriaFieldBuilder field);

    FieldSpec getField(final CriteriaFieldBuilder field);

    CodeBlock getPredicate(final CriteriaFieldBuilder field);
}
