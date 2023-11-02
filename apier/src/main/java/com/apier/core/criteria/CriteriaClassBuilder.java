package com.apier.core.criteria;

import com.apier.core.FieldUtils;
import com.apier.core.GeneratorUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class CriteriaClassBuilder {
    private final CriteriaBuilder criteriaBuilder;

    private final DeclaredType resourceType;

    private final Map<String, CriteriaFieldBuilder> fieldBuilders = new HashMap<>();

    public CriteriaClassBuilder(final CriteriaBuilder criteriaBuilder, final DeclaredType resourceType) {
        this.criteriaBuilder = criteriaBuilder;
        this.resourceType = resourceType;

        getVisibleFields().forEach(this::addField);
    }

    public String getResourceTypeName() {
        return GeneratorUtil.getTypeName(resourceType);
    }

    public String getResourceName() {
        return GeneratorUtil.getSimpleClassName(getResourceTypeName());
    }

    public String getCriteriaTypeName() {
        if (isRootCriteria()) {
            return criteriaBuilder.getControllerPackage() + "." + criteriaBuilder.getCriteriaClassName();
        } else {
            return (
                criteriaBuilder.getControllerPackage() +
                "." +
                criteriaBuilder.getCriteriaClassName() +
                "." +
                getCriteriaClassName()
            );
        }
    }

    public String getCriteriaClassName() {
        if (isRootCriteria()) {
            return criteriaBuilder.getCriteriaClassName();
        } else {
            return getResourceName() + "Criteria";
        }
    }

    public List<VariableElement> getAllFields() {
        return FieldUtils.getFields(resourceType);
    }

    public List<VariableElement> getVisibleFields() {
        return getAllFields()
            .stream()
            .filter(field -> FieldUtils.isViewMember(field, criteriaBuilder.getViewClassName()))
            .collect(Collectors.toList());
    }

    public List<CriteriaFieldBuilder> getDeepFieldBuilders() {
        return fieldBuilders.values().stream().filter(CriteriaFieldBuilder::isDeep).collect(Collectors.toList());
    }

    private CriteriaFieldBuilder addField(final VariableElement field) {
        final String fieldName = field.toString();

        if (!fieldBuilders.containsKey(fieldName)) {
            final CriteriaFieldBuilder fieldBuilder = new CriteriaFieldBuilder(this, field);
            fieldBuilders.put(fieldName, fieldBuilder);
        }

        return fieldBuilders.get(fieldName);
    }

    public TypeSpec.Builder builder() {
        final TypeSpec.Builder builder = TypeSpec
            .classBuilder(getCriteriaClassName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ClassName.bestGuess("lombok.Data"));

        fieldBuilders
            .values()
            .forEach(
                field -> {
                    CriteriaBuilder.criteriaProcessors
                        .stream()
                        .filter(processor -> processor.canApply(field))
                        .forEach(
                            processor -> {
                                builder.addField(processor.getField(field));
                            }
                        );
                }
            );

        final MethodSpec toPredicateWithParam = buildToPredicateWithParam(
            predicate -> {
                fieldBuilders
                    .values()
                    .forEach(
                        field -> {
                            CriteriaBuilder.criteriaProcessors
                                .stream()
                                .filter(processor -> processor.canApply(field))
                                .forEach(
                                    processor -> {
                                        predicate.addCode(processor.getPredicate(field));
                                    }
                                );
                        }
                    );
            }
        );

        builder.addMethod(toPredicateWithParam);
        if (isRootCriteria()) {
            builder.addMethod(buildToPredicate());
        } else {
            builder.addModifiers(Modifier.STATIC);
        }

        return builder;
    }

    private boolean isRootCriteria() {
        return criteriaBuilder.getCriteriaClassBuilder() == this;
    }

    private MethodSpec buildToPredicateWithParam(final Consumer<MethodSpec.Builder> addPredicate) {
        final ClassName booleanBuilder = ClassName.bestGuess("com.querydsl.core.BooleanBuilder");
        final MethodSpec.Builder toPredicate = MethodSpec
            .methodBuilder("toPredicate")
            .returns(ClassName.bestGuess("com.querydsl.core.types.Predicate"))
            .addParameter(getQClass(), getQClassAlias(), Modifier.FINAL)
            .addStatement("final $T predicate = new $T()", booleanBuilder, booleanBuilder);

        addPredicate.accept(toPredicate);

        toPredicate.addStatement("return predicate");

        return toPredicate.build();
    }

    private MethodSpec buildToPredicate() {
        return MethodSpec
            .methodBuilder("toPredicate")
            .returns(ClassName.bestGuess("com.querydsl.core.types.Predicate"))
            .addStatement("return toPredicate($T.$L)", getQClass(), getQClassAlias())
            .build();
    }

    private String getQTypeName() {
        return GeneratorUtil.getQClass(resourceType);
    }

    private ClassName getQClass() {
        // FIXME @RMA NPE when to deep
        return ClassName.bestGuess(getQTypeName());
    }

    public String getQClassAlias() {
        return GeneratorUtil.getQClassAlias(resourceType);
    }
}
