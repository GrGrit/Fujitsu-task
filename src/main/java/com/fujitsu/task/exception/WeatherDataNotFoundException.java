package com.fujitsu.task.exception;

/**
 * Thrown when no weather observation is available for a station at the requested time.
 */
public class WeatherDataNotFoundException extends RuntimeException {

    /**
     * Description of the missing data
     */
    public WeatherDataNotFoundException(String message) {
        super(message);
    }
}
