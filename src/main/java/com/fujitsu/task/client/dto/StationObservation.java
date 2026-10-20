package com.fujitsu.task.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record StationObservation(
        @JsonProperty("name") String name,
        @JsonProperty("wmocode") String wmoCode,
        @JsonProperty("phenomenon") String phenomenon,
        @JsonProperty("airtemperature") Double airTemperature,
        @JsonProperty("windspeed") Double windSpeed
) {
}
