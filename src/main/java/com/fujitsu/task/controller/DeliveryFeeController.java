package com.fujitsu.task.controller;

import com.fujitsu.task.dto.DeliveryFeeResponse;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * REST endpoint for delivery fee calculation.
 */
@RestController
@RequestMapping("/api/delivery-fee")
@RequiredArgsConstructor
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    /**
     * Calculates the delivery fee for a city and vehicle type.
     */
    @GetMapping
    public DeliveryFeeResponse getDeliveryFee(@RequestParam City city,
                                              @RequestParam VehicleType vehicleType,
                                              @RequestParam(required = false) Instant dateTime) {
        BigDecimal fee = deliveryFeeService.calculateFee(city, vehicleType, dateTime);
        return new DeliveryFeeResponse(city, vehicleType, fee);
    }
}
