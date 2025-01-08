package org.omn3s.userservice.utils;

/**
 * Exception to signal when an attempt to create a duplicate entry has been made.
 */
public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
