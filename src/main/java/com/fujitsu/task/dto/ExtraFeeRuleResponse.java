package com.fujitsu.task.dto;

import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A weather-based extra fee rule.
 */
public record ExtraFeeRuleResponse(
        Long id,
        WeatherConditionType conditionType,
        VehicleType vehicleType,
        BigDecimal minValue,
        boolean minInclusive,
        BigDecimal maxValue,
        boolean maxInclusive,
        String phenomenonKeyword,
        BigDecimal fee,
        boolean forbidden,
        Instant validFrom,
        Instant validTo
) {
}
