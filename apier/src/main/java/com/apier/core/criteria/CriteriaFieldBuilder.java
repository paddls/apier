package com.apier.core.criteria;

import com.apier.core.FieldUtils;
import com.apier.core.GeneratorUtil;
import lombok.Getter;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Arrays;

@Getter
public class CriteriaFieldBuilder {
    private final VariableElement field;

    private final CriteriaClassBuilder criteriaClassBuilder;

    public CriteriaFieldBuilder(final CriteriaClassBuilder criteriaClassBuilder, final VariableElement field) {
        this.criteriaClassBuilder = criteriaClassBuilder;
        this.field = field;
    }

    public String getName() {
        return field.toString();
    }

    public boolean isDeep() {
        return !FieldUtils
                .getAnnotations(field, Arrays.asList("jakarta.persistence.ManyToOne", "jakarta.persistence.OneToOne"))
            .isEmpty();
    }

    public DeclaredType getResourceType() {
        return (DeclaredType) field.asType();
    }

    public String getTypeName() {
        return GeneratorUtil.getTypeName(getResourceType());
    }

    public String getCriteriaTypeName() {
        final CriteriaClassBuilder criteriaBuilder = criteriaClassBuilder
            .getCriteriaBuilder()
            .getClassBuilders()
            .get(getTypeName());

        return criteriaBuilder.getCriteriaTypeName();
    }
}
