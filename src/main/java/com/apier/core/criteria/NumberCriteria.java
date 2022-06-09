package com.apier.core.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.Data;

@Data
public class NumberCriteria<T extends Number & Comparable<?>> {
    private T eq;

    private T gt;

    private T lt;

    private T goe;

    private T loe;

    public Predicate toPredicate(final NumberPath<T> number) {
        final BooleanBuilder predicate = new BooleanBuilder();

        if (eq != null) {
            predicate.and(number.eq(eq));
        }
        if (gt != null) {
            predicate.and(number.gt(gt));
        }
        if (lt != null) {
            predicate.and(number.lt(lt));
        }
        if (goe != null) {
            predicate.and(number.goe(goe));
        }
        if (loe != null) {
            predicate.and(number.loe(loe));
        }

        return predicate;
    }
}
