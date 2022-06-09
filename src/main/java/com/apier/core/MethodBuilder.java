package com.apier.core;

import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.ExecutableElement;

@FunctionalInterface
public interface MethodBuilder {
    void build(final MethodSpec.Builder method, final ExecutableElement api, final OperationEnum operation);
}
