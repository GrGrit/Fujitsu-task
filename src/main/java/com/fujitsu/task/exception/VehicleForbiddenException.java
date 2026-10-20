package com.fujitsu.task.exception;

/**
 * Thrown when weather conditions do not allow the selected vehicle type to be used.
 */
public class VehicleForbiddenException extends RuntimeException {

    public static final String MESSAGE = "Usage of selected vehicle type is forbidden";

    /**
     * Creates the exception with the error message required by the specification.
     */
    public VehicleForbiddenException() {
        super(MESSAGE);
    }
}
