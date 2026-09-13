package com.carex.exception;

/**
 * Thrown when an operation violates a defined CAREX business rule.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>"Online and offline appointments require different doctors"</li>
 *   <li>"Doctor is not active and cannot accept new appointments"</li>
 * </ul>
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
