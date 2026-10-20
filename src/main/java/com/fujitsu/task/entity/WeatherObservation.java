package com.fujitsu.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A single weather observation of one station.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "weather_observation",
        indexes = @Index(name = "idx_station_timestamp", columnList = "stationName, observationTimestamp"))
public class WeatherObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String stationName;
    private String wmoCode;
    private Double airTemperature;
    private Double windSpeed;
    private String weatherPhenomenon;
    @Column(nullable = false)
    private Instant observationTimestamp;

    /**
     * Creates a new observation.
     */
    public WeatherObservation(String stationName, String wmoCode, Double airTemperature, Double windSpeed,
                              String weatherPhenomenon, Instant observationTimestamp) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperature = airTemperature;
        this.windSpeed = windSpeed;
        this.weatherPhenomenon = weatherPhenomenon;
        this.observationTimestamp = observationTimestamp;
    }
}
