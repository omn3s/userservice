package org.omn3s.userservice.utils;

public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
