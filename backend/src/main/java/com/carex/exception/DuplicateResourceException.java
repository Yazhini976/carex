package com.carex.exception;

/**
 * Thrown when attempting to create a resource that already exists.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>"User with email already exists: john@example.com"</li>
 *   <li>"Specialty 'Cardiology' already exists"</li>
 *   <li>"Doctor-specialty mapping already exists"</li>
 * </ul>
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException of(String entityName, String field, String value) {
        return new DuplicateResourceException(
                entityName + " with " + field + " already exists: " + value);
    }
}
