package com.apier.core.criteria;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalDateTimeCriteria extends DateTimeCriteria<LocalDateTime> {
}
