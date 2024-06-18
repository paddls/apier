package com.apier.core.criteria.querydsl.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanPath;
import lombok.Data;

@Data
public class QBooleanCriteria {
    private Boolean eq;

    public Predicate toPredicate(final BooleanPath booleanPath) {
        final BooleanBuilder predicate = new BooleanBuilder();

        if (eq != null) {
            predicate.and(booleanPath.eq(eq));
        }

        return predicate;
    }
}
