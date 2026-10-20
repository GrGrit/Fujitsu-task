package com.fujitsu.task.exception;

/**
 * Thrown when rule data is consistent field by field but invalid as a whole (e.g. min greater than max).
 */
public class InvalidFeeRuleException extends RuntimeException {

    /**
     * Description of the problem
     */
    public InvalidFeeRuleException(String message) {
        super(message);
    }
}
