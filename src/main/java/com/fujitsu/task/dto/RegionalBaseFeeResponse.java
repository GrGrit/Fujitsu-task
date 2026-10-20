package com.fujitsu.task.dto;

import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A regional base fee rule.
 */
public record RegionalBaseFeeResponse(
        Long id,
        City city,
        VehicleType vehicleType,
        BigDecimal fee,
        Instant validFrom,
        Instant validTo
) {
}
