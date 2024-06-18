package com.apier.core.criteria.querydsl.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class QLocalDateTimeCriteria extends QDateTimeCriteria<LocalDateTime> {
}
