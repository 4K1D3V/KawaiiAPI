package dev.oumaimaa.kawaiiapi.exceptions;

import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when a configuration error occurs.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new ConfigurationException with no detail message.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message The detail message
     */
    public ConfigurationException(@Nullable String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of this exception
     */
    public ConfigurationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ConfigurationException with the specified cause.
     *
     * @param cause The cause of this exception
     */
    public ConfigurationException(@Nullable Throwable cause) {
        super(cause);
    }
}
