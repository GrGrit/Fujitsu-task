package com.fujitsu.task.mapper;

import com.fujitsu.task.client.dto.StationObservation;
import com.fujitsu.task.entity.WeatherObservation;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts weather data from the external weather service format into {@link WeatherObservation} entities.
 */
@Component
public class WeatherObservationMapper {

    /**
     * Creates a new, unsaved observation entity from a station element of the observations XML.
     */
    public WeatherObservation toEntity(StationObservation station, Instant timestamp) {
        return new WeatherObservation(
                station.name(),
                station.wmoCode(),
                station.airTemperature(),
                station.windSpeed(),
                station.phenomenon(),
                timestamp);
    }
}
