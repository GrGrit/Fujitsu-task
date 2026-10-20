package com.fujitsu.task.exception;

/**
 * Thrown when no fee rule exists for the requested parameters.
 */
public class FeeRuleNotFoundException extends RuntimeException {

    /**
     * Description of the missing rule
     */
    public FeeRuleNotFoundException(String message) {
        super(message);
    }
}
