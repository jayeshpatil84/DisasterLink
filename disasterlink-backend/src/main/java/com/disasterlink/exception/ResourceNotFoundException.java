package com.disasterlink.exception;

/**
 * Thrown when a requested entity (user, report, etc.) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
