package com.fujitsu.task.dto;

import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Data for creating or updating a weather-based extra fee rule.
 */
public record ExtraFeeRuleRequest(
        @NotNull WeatherConditionType conditionType,
        @NotNull VehicleType vehicleType,
        @Digits(integer = 8, fraction = 2) BigDecimal minValue,
        boolean minInclusive,
        @Digits(integer = 8, fraction = 2) BigDecimal maxValue,
        boolean maxInclusive,
        String phenomenonKeyword,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal fee,
        boolean forbidden
) {
}
