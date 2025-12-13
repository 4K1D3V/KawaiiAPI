package dev.oumaimaa.kawaiiapi.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when the Menu Manager has not been properly initialized.
 * This typically occurs when attempting to use menu functions before calling MenuManager.setup().
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public class MenuManagerNotSetupException extends Exception {

    /**
     * Constructs a new MenuManagerNotSetupException with a default message.
     */
    public MenuManagerNotSetupException() {
        super("MenuManager has not been initialized. Call MenuManager.setup() during plugin startup.");
    }

    /**
     * Constructs a new MenuManagerNotSetupException with a custom message.
     *
     * @param message The detail message
     */
    public MenuManagerNotSetupException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new MenuManagerNotSetupException with a message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of this exception
     */
    public MenuManagerNotSetupException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}