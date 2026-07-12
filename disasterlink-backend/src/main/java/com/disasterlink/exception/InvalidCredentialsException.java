package com.disasterlink.exception;

/**
 * Thrown when a login attempt fails due to an incorrect username or password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
