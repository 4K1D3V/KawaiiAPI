package dev.oumaimaa.kawaiiapi.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when a chat input operation fails or times out.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
class ChatInputException extends RuntimeException {

    /**
     * Constructs a new ChatInputException with the specified detail message.
     *
     * @param message The detail message
     */
    public ChatInputException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new ChatInputException with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of this exception
     */
    public ChatInputException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}