package com.apier.core.criteria;

import com.apier.core.GeneratorUtil;
import com.apier.core.criteria.querydsl.processor.*;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class CriteriaBuilder {
    public static final List<CriteriaProcessor> criteriaProcessors = Arrays.asList(
            new QStringProcessor(),
            new QNumberCriteriaProcessor(),
            new QDateTimeCriteriaProcessor(),
            new QLocalDateTimeCriteriaProcessor(),
            new QBooleanCriteriaProcessor(),
            new QZonedDateTimeCriteriaProcessor(),
        new DeepCriteriaProcessor()
    );

    private final ExecutableElement api;

    private final CriteriaClassBuilder criteriaClassBuilder;

    private final Map<String, CriteriaClassBuilder> classBuilders = new HashMap<>();

    public CriteriaBuilder(final ExecutableElement api) {
        this.api = api;
        this.criteriaClassBuilder = addCriteriaClass(getResourceType());
    }

    private DeclaredType getResourceType() {
        return ((DeclaredType) api.getReturnType()).getTypeArguments()
            .stream()
            .findFirst()
            .filter(DeclaredType.class::isInstance)
            .map(DeclaredType.class::cast)
            .orElseThrow(() -> new RuntimeException("unable to get return type from function"));
    }

    private List<CriteriaClassBuilder> getChildCriteriaClassBuilders() {
        return classBuilders
            .values()
            .stream()
            .filter(builder -> builder != criteriaClassBuilder)
            .collect(Collectors.toList());
    }

    private CriteriaClassBuilder addCriteriaClass(final DeclaredType resourceType) {
        final String resourceTypeName = GeneratorUtil.getTypeName(resourceType);

        if (!classBuilders.containsKey(resourceTypeName)) {
            final CriteriaClassBuilder criteriaClassBuilder = new CriteriaClassBuilder(this, resourceType);

            classBuilders.put(resourceTypeName, criteriaClassBuilder);

            criteriaClassBuilder
                .getDeepFieldBuilders()
                .forEach(
                    fieldBuilder -> {
                        addCriteriaClass(fieldBuilder.getResourceType());
                    }
                );
        }

        return classBuilders.get(resourceTypeName);
    }

    private String getControllerName() {
        return api.getEnclosingElement().getSimpleName().toString().replace("ControllerImpl", "");
    }

    public String getControllerPackage() {
        return api.getEnclosingElement().getEnclosingElement().toString();
    }

    public String getCriteriaClassName() {
        return getControllerName() + "Criteria";
    }

    public String getViewClassName() {
        return getControllerName() + "View";
    }

    public TypeSpec build() {
        final TypeSpec.Builder criteriaClassBuilder = this.criteriaClassBuilder.builder();

        getChildCriteriaClassBuilders()
            .forEach(
                child -> {
                    criteriaClassBuilder.addType(child.builder().build());
                }
            );

        return criteriaClassBuilder.build();
    }
}
