package com.fujitsu.task.dto;

import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Data for creating or updating a regional base fee.
 */
public record RegionalBaseFeeRequest(
        @NotNull City city,
        @NotNull VehicleType vehicleType,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal fee
) {
}
