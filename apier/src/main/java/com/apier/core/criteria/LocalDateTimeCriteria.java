package com.apier.core.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalDateTimeCriteria extends DateTimeCriteria<LocalDateTime> {
}
