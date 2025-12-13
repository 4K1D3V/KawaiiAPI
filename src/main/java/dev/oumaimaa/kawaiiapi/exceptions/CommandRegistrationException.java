package dev.oumaimaa.kawaiiapi.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a command registration error occurs.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
class CommandRegistrationException extends RuntimeException {

    /**
     * Constructs a new CommandRegistrationException with the specified detail message.
     *
     * @param message The detail message
     */
    public CommandRegistrationException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new CommandRegistrationException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of this exception
     */
    public CommandRegistrationException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
