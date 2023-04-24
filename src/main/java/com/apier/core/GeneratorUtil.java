package com.apier.core;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Attribute;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class GeneratorUtil {

    public static String getViewClassName(final ExecutableElement api) {
        return api.getEnclosingElement().getSimpleName().toString().replace("Controller", "View");
    }

    public static Set<String> getViewClassNames(final VariableElement field) {
        final Optional<Attribute.Compound> jsonViewAnnotation = FieldUtils.getAnnotation(
                field,
                "com.fasterxml.jackson.annotation.JsonView"
        );

        final Optional<Attribute> jsonViewAttribute = jsonViewAnnotation
                .map(Attribute.Compound::getElementValues)
                .flatMap(
                        params ->
                                params
                                        .entrySet()
                                        .stream()
                                        .filter(param -> param.getKey().toString().equals("value()"))
                                        .map(Map.Entry::getValue)
                                        .findFirst()
                );

        return (Set<String>) jsonViewAttribute
                .map(Attribute::getValue)
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .map(
                        views ->
                                Stream
                                        .concat(
                                                views
                                                        .stream()
                                                        .filter(Attribute.Class.class::isInstance)
                                                        .map(attribute -> ((Attribute.Class) attribute).classType)
                                                        .map(classType -> GeneratorUtil.getSimpleClassName(classType.toString())),
                                                views
                                                        .stream()
                                                        .filter(Attribute.UnresolvedClass.class::isInstance)
                                                        .map(attribute -> ((Attribute.UnresolvedClass) attribute).classType)
                                                        .map(classType -> classType.toString().replace("<any?>.", ""))
                                        )
                                        .collect(Collectors.toSet())
                )
                .orElse(new HashSet<>());
    }

    public static String getPackageName(final String fullClassName) {
        final List<String> elements = Arrays.asList(fullClassName.split("\\."));
        final List<String> packages = elements.subList(0, elements.size() - 1);

        return String.join(".", packages);
    }

    public static String getSimpleClassName(final String fullClassName) {
        return fullClassName.replaceAll("^[a-z0-9\\.]*", "");
    }

    public static String getTypeName(final DeclaredType resourceType) {
        return resourceType.asElement().toString();
    }

    public static String getQClassAlias(final DeclaredType resourceClass) {
        final String resourceClassName = getSimpleClassName(getTypeName(resourceClass)); // TODO check when suffix with number (ex: user1)

        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, resourceClassName);
    }

    public static String getQClass(final DeclaredType resourceClass) {
        final String resourceTypeName = getTypeName(resourceClass);
        final String resourceClassName = getSimpleClassName(resourceTypeName);
        final String resourcePackage = getPackageName(resourceTypeName);

        return String.format("%s.Q%s", resourcePackage, resourceClassName);
    }

    public static Optional<? extends AnnotationValue> getAnnotationValue(
            final ExecutableElement api,
            final String annotationType
    ) {
        return api
                .getAnnotationMirrors()
                .stream()
                .filter(annotation -> annotation.getAnnotationType().toString().equals(annotationType))
                .map(AnnotationMirror::getElementValues)
                .map(Map::values)
                .map(values -> values.stream().findFirst().orElse(null))
                .findFirst();
    }

    public static ClassName getResourceType(final List<ExecutableElement> apis) {
        return apis
                .stream()
                .map(ExecutableElement.class::cast)
                .map(ExecutableElement::getReturnType)
                .map(TypeMirror::toString)
                .map(GeneratorUtil::getResourceType)
                .findFirst()
                .orElse(null);
    }

    public static ClassName getResourceType(final String name) {
        final List<String> names = Arrays.asList(name.split("[<>]"));

        return ClassName.bestGuess(names.get(names.size() - 1));
    }

    public static String normalizeMethodName(final String methodName) {
        return Arrays
                .stream(OperationEnum.values())
                .filter(operation -> methodName.startsWith(operation.getMethodName()))
                .findFirst()
                .map(OperationEnum::getMethodName)
                .orElseThrow(() -> new RuntimeException("Method name not supported: " + methodName));
    }

    public static boolean isPageApi(final Element apiElement) {
        return ((ExecutableElement) apiElement).getReturnType()
                .toString()
                .startsWith("org.springframework.data.domain.Page");
    }

    public static TypeName getTypeName(final String name) {
        final List<String> names = Arrays.asList(name.split("[<>]"));

        if (names.get(0).equals("void")) {
            return ClassName.VOID;
        } else if (names.size() == 1) {
            return ClassName.bestGuess(names.get(0));
        } else { // TODO @RMA multiple params support
            return ParameterizedTypeName.get(ClassName.bestGuess(names.get(0)), ClassName.bestGuess(names.get(1)));
        }
    }
}
