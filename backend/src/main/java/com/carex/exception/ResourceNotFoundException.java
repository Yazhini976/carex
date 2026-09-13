package com.carex.exception;

/**
 * Thrown when a requested resource cannot be found by its identifier.
 *
 * <p>Example: "Patient not found with id: 12"</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Long id) {
        return new ResourceNotFoundException(entityName + " not found with id: " + id);
    }

    public static ResourceNotFoundException of(String entityName, String field, String value) {
        return new ResourceNotFoundException(entityName + " not found with " + field + ": " + value);
    }
}
