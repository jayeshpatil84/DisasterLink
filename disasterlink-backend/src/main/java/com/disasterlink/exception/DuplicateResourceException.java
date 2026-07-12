package com.disasterlink.exception;

/**
 * Thrown when trying to create a resource that already exists
 * (e.g. registering with a username or email that is already taken).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
