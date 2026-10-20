package com.fujitsu.task.exception;

/**
 * Thrown when creating a rule would result in two rules in force for the same parameters.
 */
public class DuplicateFeeRuleException extends RuntimeException {

    /**
     * Description of the conflict
     */
    public DuplicateFeeRuleException(String message) {
        super(message);
    }
}
