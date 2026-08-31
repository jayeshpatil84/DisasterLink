package com.disasterlink.exception;

/**
 * Thrown when an SOS status change violates the allowed transition order:
 *   PENDING → ASSIGNED → EN_ROUTE → ARRIVED → IN_PROGRESS → RESOLVED
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
