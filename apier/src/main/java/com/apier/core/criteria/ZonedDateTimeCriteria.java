package com.apier.core.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZonedDateTimeCriteria extends DateTimeCriteria<ZonedDateTime> {
}
