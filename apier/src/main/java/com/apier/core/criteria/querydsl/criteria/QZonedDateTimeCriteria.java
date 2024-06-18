package com.apier.core.criteria.querydsl.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class QZonedDateTimeCriteria extends QDateTimeCriteria<ZonedDateTime> {
}
