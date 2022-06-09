package com.apier.core.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.DateTimePath;
import lombok.Data;

@Data
public class DateTimeCriteria<T extends Comparable<?>> {
    private T eq;

    private T gt;

    private T lt;

    private T goe;

    private T loe;

    public Predicate toPredicate(final DateTimePath<T> dateTime) {
        final BooleanBuilder predicate = new BooleanBuilder();

        if (eq != null) {
            predicate.and(dateTime.eq(eq));
        }
        if (gt != null) {
            predicate.and(dateTime.gt(gt));
        }
        if (lt != null) {
            predicate.and(dateTime.lt(lt));
        }
        if (goe != null) {
            predicate.and(dateTime.goe(goe));
        }
        if (loe != null) {
            predicate.and(dateTime.loe(loe));
        }

        return predicate;
    }
}
