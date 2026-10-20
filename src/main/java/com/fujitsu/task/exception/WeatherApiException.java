package com.fujitsu.task.exception;


public class WeatherApiException extends RuntimeException {

    /**
     * @param message description of the failure
     */
    public WeatherApiException(String message) {
        super(message);
    }

    /**
     * @param message description of the failure
     * @param cause   underlying exception
     */
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
