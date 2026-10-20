package com.fujitsu.task.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

/**
 * Standard Spring MVC errors.
 * (missing parameters, invalid request bodies, etc.) are handled by the base class.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * @param e exception thrown when weather forbids the vehicle type
     * @return 422 Unprocessable Content with the message required by the specification
     */
    @ExceptionHandler(VehicleForbiddenException.class)
    public ProblemDetail handleVehicleForbidden(VehicleForbiddenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    /**
     * @param e exception thrown when a rule does not exist
     * @return 404 Not Found
     */
    @ExceptionHandler(FeeRuleNotFoundException.class)
    public ProblemDetail handleFeeRuleNotFound(FeeRuleNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * @param e exception thrown when no weather data is available yet
     * @return 503 Service Unavailable
     */
    @ExceptionHandler(WeatherDataNotFoundException.class)
    public ProblemDetail handleWeatherDataNotFound(WeatherDataNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    /**
     * @param e exception thrown when a rule would conflict with an existing one
     * @return 409 Conflict
     */
    @ExceptionHandler(DuplicateFeeRuleException.class)
    public ProblemDetail handleDuplicateFeeRule(DuplicateFeeRuleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * @param e exception thrown when rule data is inconsistent
     * @return 400 Bad Request
     */
    @ExceptionHandler(InvalidFeeRuleException.class)
    public ProblemDetail handleInvalidFeeRule(InvalidFeeRuleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Gives a readable message for invalid request parameters, listing allowed values for enums.
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String parameter = e instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName() : e.getPropertyName();
        String detail = "Invalid value '" + e.getValue() + "' for parameter '" + parameter + "'";
        Class<?> requiredType = e.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            detail += ", allowed values: " + Arrays.toString(requiredType.getEnumConstants());
        }
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        return handleExceptionInternal(e, body, headers, status, request);
    }
}
