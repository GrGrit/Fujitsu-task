package com.fujitsu.task.controller;

import com.fujitsu.task.dto.DeliveryFeeResponse;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.service.DeliveryFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * REST endpoint for delivery fee calculation.
 */
@Tag(name = "Delivery fee", description = "Delivery fee calculation")
@RestController
@RequestMapping("/api/delivery-fee")
@RequiredArgsConstructor
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    /**
     * Calculates the delivery fee for a city and vehicle type.
     */
    @Operation(
            summary = "Calculate delivery fee",
            description = "Total fee = regional base fee + weather-based extra fees, using the latest weather "
                    + "observation of the city. With `dateTime`, the rules and weather data valid at that time are used.")
    @ApiResponse(responseCode = "200", description = "Fee calculated")
    @ApiResponse(responseCode = "400", description = "Missing or invalid parameter",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No base fee rule for the city and vehicle type",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Usage of selected vehicle type is forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "No weather data available for the city",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public DeliveryFeeResponse getDeliveryFee(
            @Parameter(description = "Delivery city", example = "TARTU")
            @RequestParam City city,
            @Parameter(description = "Courier vehicle type", example = "BIKE")
            @RequestParam VehicleType vehicleType,
            @Parameter(description = "Optional ISO-8601 point in time; current time if omitted",
                    example = "2026-01-10T12:00:00Z")
            @RequestParam(required = false) Instant dateTime) {
        BigDecimal fee = deliveryFeeService.calculateFee(city, vehicleType, dateTime);
        return new DeliveryFeeResponse(city, vehicleType, fee);
    }
}
