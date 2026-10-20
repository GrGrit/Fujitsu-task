package com.fujitsu.task.dto;

import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;

import java.math.BigDecimal;

/**
 * Calculated delivery fee.
 */
public record DeliveryFeeResponse(City city, VehicleType vehicleType, BigDecimal fee) {
}
