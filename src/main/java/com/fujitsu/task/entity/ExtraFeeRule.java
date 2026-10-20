package com.fujitsu.task.entity;

import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Weather-based extra fee rule for one vehicle type.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "extra_fee_rule")
public class ExtraFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeatherConditionType conditionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(precision = 10, scale = 2)
    private BigDecimal minValue;
    private boolean minInclusive;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxValue;
    private boolean maxInclusive;

    private String phenomenonKeyword;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    private boolean forbidden;

    @Column(nullable = false)
    private Instant validFrom;

    private Instant validTo;
}
