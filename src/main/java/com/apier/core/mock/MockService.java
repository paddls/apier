package com.apier.core.mock;

import com.apier.core.GeneratorUtil;
import com.apier.core.MethodBuilder;
import com.apier.core.OperationEnum;
import com.google.common.base.Strings;
import com.squareup.javapoet.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockService {
    private final Mock config;

    private final ClassName serviceClassName;

    private final List<ExecutableElement> apis;

    public TypeSpec.Builder builder() {
        final TypeName resourceType = GeneratorUtil.getResourceType(apis);
        final TypeSpec.Builder mockServiceClass = TypeSpec
                .classBuilder(serviceClassName.simpleName())
                .addAnnotation(ClassName.get("org.springframework.stereotype", "Service"))
                .addModifiers(Modifier.PUBLIC)
                .addField(
                        FieldSpec
                                .builder(ParameterizedTypeName.get(ClassName.get(List.class), resourceType), "values")
                                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                .initializer(CodeBlock.builder().add("new $T()", ArrayList.class).build())
                                .build()
                );

        if (!Strings.isNullOrEmpty(config.value())) {
            mockServiceClass.addMethod(
                    MethodSpec
                            .constructorBuilder()
                            .addException(Exception.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(
                                    ParameterSpec
                                            .builder(ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper"), "mapper")
                                            .addModifiers(Modifier.FINAL)
                                            .build()
                            )
                            .addStatement(
                                    "final $T[] data = new $T($S).getInputStream().readAllBytes()",
                                    byte.class,
                                    ClassName.bestGuess("org.springframework.core.io.ClassPathResource"),
                                    config.value()
                            )
                            .addStatement(
                                    "values.addAll($T.asList(mapper.readValue(new $T(data), $T[].class)))",
                                    Arrays.class,
                                    String.class,
                                    resourceType
                            )
                            .build()
            );
        }

        apis.forEach(api -> addMockMethods(mockServiceClass, api));

        return mockServiceClass;
    }

    private void addMockMethods(final TypeSpec.Builder mockServiceClass, final Element apiElement) {
        final String methodName = GeneratorUtil.normalizeMethodName(apiElement.getSimpleName().toString());

        switch (methodName) {
            case "findById":
                addMockMethod(
                        mockServiceClass,
                        (ExecutableElement) apiElement,
                        OperationEnum.DETAIL,
                        (method, api, operation) -> {
                            method.addStatement("return $T.ofNullable(values.get(0))", Optional.class);
                        }
                );
                break;
            case "findAll":
                addMockMethod(
                        mockServiceClass,
                        (ExecutableElement) apiElement,
                        OperationEnum.LIST,
                        (method, api, operation) -> {
                            if (GeneratorUtil.isPageApi(api)) {
                                method.addStatement(
                                        "return new $T<>(values)",
                                        ClassName.bestGuess("org.springframework.data.domain.PageImpl")
                                );
                            } else {
                                method.addStatement("return values");
                            }
                        }
                );
                break;
            case "create":
                addMockMethod(
                        mockServiceClass,
                        (ExecutableElement) apiElement,
                        OperationEnum.CREATE,
                        (method, api, operation) -> {
                            method.addStatement("return values.get(0)");
                        }
                );
                break;
            case "update":
                addMockMethod(
                        mockServiceClass,
                        (ExecutableElement) apiElement,
                        OperationEnum.UPDATE,
                        (method, api, operation) -> {
                            method.addStatement("return values.get(0)");
                        }
                );
                break;
            case "patch":
                addMockMethod(
                        mockServiceClass,
                        (ExecutableElement) apiElement,
                        OperationEnum.PATCH,
                        (method, api, operation) -> {
                            method.addStatement("return values.get(0)");
                        }
                );
                break;
            case "delete":
                addMockMethod(mockServiceClass, (ExecutableElement) apiElement, OperationEnum.DELETE);
                break;
        }
    }

    private void addMockMethod(
            final TypeSpec.Builder controllerClass,
            final ExecutableElement api,
            final OperationEnum operation,
            final MethodBuilder... methodBuilder
    ) {
        final MethodSpec.Builder method = createMockMethod(api, operation);

        Arrays.stream(methodBuilder).forEach(builder -> builder.build(method, api, operation));

        controllerClass.addMethod(method.build());
    }

    private MethodSpec.Builder createMockMethod(final ExecutableElement api, final OperationEnum operation) {
        final String methodName = api.getSimpleName().toString();
        final TypeName returnType = GeneratorUtil.getTypeName(api.getReturnType().toString());

        final TypeName returns = OperationEnum.DETAIL.equals(operation)
                ? ParameterizedTypeName.get(ClassName.get(Optional.class), returnType)
                : returnType;

        return MethodSpec
                .methodBuilder(methodName)
                .returns(returns)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(
                        api
                                .getParameters()
                                .stream()
                                .map(
                                        param -> {
                                            final ParameterSpec.Builder parameter = ParameterSpec.builder(
                                                    ClassName.bestGuess(param.asType().toString()),
                                                    param.toString(),
                                                    Modifier.FINAL
                                            );

                                            return parameter.build();
                                        }
                                )
                                .collect(Collectors.toList())
                );
    }
}
