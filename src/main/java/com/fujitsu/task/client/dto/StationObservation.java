package com.fujitsu.task.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single {@code <station>} element of the observations XML; empty elements are mapped to null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StationObservation(
        @JsonProperty("name") String name,
        @JsonProperty("wmocode") String wmoCode,
        @JsonProperty("phenomenon") String phenomenon,
        @JsonProperty("airtemperature") Double airTemperature,
        @JsonProperty("windspeed") Double windSpeed
) {
}
