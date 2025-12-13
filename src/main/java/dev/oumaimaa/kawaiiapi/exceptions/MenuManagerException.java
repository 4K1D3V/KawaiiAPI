package dev.oumaimaa.kawaiiapi.exceptions;

import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when an error occurs within the Menu Manager system.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public class MenuManagerException extends Exception {

    /**
     * Constructs a new MenuManagerException with no detail message.
     */
    public MenuManagerException() {
        super();
    }

    /**
     * Constructs a new MenuManagerException with the specified detail message.
     *
     * @param message The detail message
     */
    public MenuManagerException(@Nullable String message) {
        super(message);
    }

    /**
     * Constructs a new MenuManagerException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of this exception
     */
    public MenuManagerException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MenuManagerException with the specified cause.
     *
     * @param cause The cause of this exception
     */
    public MenuManagerException(@Nullable Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MenuManagerException with all exception details.
     *
     * @param message            The detail message
     * @param cause              The cause of this exception
     * @param enableSuppression  Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    protected MenuManagerException(@Nullable String message,
                                   @Nullable Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}