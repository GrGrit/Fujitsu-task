package com.fujitsu.task.model;

/**
 * Weather condition an extra fee rule is based on.
 */
public enum WeatherConditionType {

    /**
     * Air temperature in °C, matched against the rule's numeric range.
     */
    AIR_TEMPERATURE,

    /**
     * Wind speed in m/s, matched against the rule's numeric range.
     */
    WIND_SPEED,

    /**
     * Matched if it contains the rule's keyword (case-insensitive).
     */
    WEATHER_PHENOMENON
}
