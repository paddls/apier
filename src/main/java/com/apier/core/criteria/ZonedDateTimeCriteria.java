package com.apier.core.criteria;

import java.time.ZonedDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZonedDateTimeCriteria extends DateTimeCriteria<ZonedDateTime> {
}
