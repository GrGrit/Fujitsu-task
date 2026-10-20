package com.fujitsu.task.exception;

/**
 * Thrown when weather data cannot be fetched from or parsed from the external weather service.
 */
public class WeatherApiException extends RuntimeException {

    /**
     * @param message description of the failure
     */
    public WeatherApiException(String message) {
        super(message);
    }

    /**
     * Description of the failure
     * @param cause   underlying exception
     */
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
