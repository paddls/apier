package com.apier.core.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Data;

@Data
public class StringCriteria {
    private String eq;

    private String like;

    public Predicate toPredicate(final StringPath string) {
        final BooleanBuilder predicate = new BooleanBuilder();

        if (eq != null) {
            predicate.and(string.eq(eq));
        }
        if (like != null) {
            predicate.and(string.containsIgnoreCase(like));
        }

        return predicate;
    }
}
