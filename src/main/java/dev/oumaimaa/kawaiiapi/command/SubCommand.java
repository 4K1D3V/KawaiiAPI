package dev.oumaimaa.kawaiiapi.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Abstract base class for subcommands within a core command structure.
 * Example: In /chunkcollector buy, "buy" would be the subcommand.
 *
 * <p>Subcommands should be lightweight, stateless, and thread-safe.
 * Each subcommand is instantiated once per command registration.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public abstract class SubCommand {

    /**
     * Gets the name of this subcommand.
     * Must be lowercase and contain no spaces.
     *
     * @return The subcommand name (case-insensitive during matching)
     */
    @NotNull
    public abstract String getName();

    /**
     * Gets the aliases that can be used for this subcommand.
     * Aliases are alternative names that trigger the same subcommand.
     *
     * @return List of aliases, or null if no aliases exist
     */
    @Nullable
    public abstract List<String> getAliases();

    /**
     * Gets a description of what this subcommand does.
     * Displayed in the default command list.
     *
     * @return A user-friendly description
     */
    @NotNull
    public abstract String getDescription();

    /**
     * Gets the syntax/usage example for this subcommand.
     * Should include the full command path and argument placeholders.
     *
     * @return Usage example (e.g., "/command subcommand &lt;arg&gt; [optional]")
     */
    @NotNull
    public abstract String getSyntax();

    /**
     * Executes the subcommand logic.
     * Called on the main server thread.
     *
     * @param sender The entity that executed the command (Player, Console, etc.)
     * @param args   The command arguments (includes the subcommand name at index 0)
     */
    public abstract void perform(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Provides tab completion suggestions for this subcommand's arguments.
     * Called asynchronously for tab completion.
     *
     * @param sender The entity requesting tab completion
     * @param args   The current command arguments
     * @return List of suggestions for tab completion, or null for no suggestions
     */
    @Nullable
    public abstract List<String> getSubcommandArguments(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Gets the permission node required to use this subcommand.
     * If null or empty, no permission is required.
     *
     * @return The permission node, or null for no permission requirement
     */
    @Nullable
    public String getPermission() {
        return null; // Default: no permission required
    }

    /**
     * Checks if this subcommand can only be executed by players.
     * Override to return true if console/command blocks should be blocked.
     *
     * @return true if player-only, false to allow all senders
     */
    public boolean isPlayerOnly() {
        return false; // Default: allow all senders
    }

    /**
     * Gets the minimum number of arguments required (excluding the subcommand name).
     * Override to enable automatic argument count validation.
     *
     * @return Minimum argument count, or 0 for no minimum
     */
    public int getMinimumArguments() {
        return 0; // Default: no minimum
    }

    /**
     * Gets the maximum number of arguments allowed (excluding the subcommand name).
     * Override to enable automatic argument count validation.
     *
     * @return Maximum argument count, or -1 for unlimited
     */
    public int getMaximumArguments() {
        return -1; // Default: unlimited
    }

    /**
     * Called before perform() to validate the command execution.
     * Override to add custom validation logic.
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return true if validation passed, false to cancel execution
     */
    protected boolean validate(@NotNull CommandSender sender, @NotNull String[] args) {
        return true; // Default: always valid
    }

    /**
     * Gets the cooldown time for this subcommand in milliseconds.
     * Override to implement per-subcommand cooldowns.
     *
     * @return Cooldown time in milliseconds, or 0 for no cooldown
     */
    public long getCooldown() {
        return 0; // Default: no cooldown
    }

    /**
     * Checks if this subcommand requires confirmation before execution.
     * Useful for destructive operations.
     *
     * @return true if confirmation is required
     */
    public boolean requiresConfirmation() {
        return false; // Default: no confirmation
    }

    /**
     * Gets the message to display when asking for confirmation.
     * Only used if requiresConfirmation() returns true.
     *
     * @return The confirmation message
     */
    @NotNull
    public String getConfirmationMessage() {
        return "§eAre you sure? Type §a/" + getName() + " confirm §eto proceed.";
    }

    /**
     * Utility method to check if sender has a specific permission.
     *
     * @param sender     The command sender
     * @param permission The permission to check
     * @return true if sender has permission
     */
    protected boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }

    /**
     * Utility method to send a colored message to the sender.
     *
     * @param sender  The message recipient
     * @param message The message with color codes
     */
    protected void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(message.replace('&', '§'));
    }

    /**
     * Utility method to send an error message with standard formatting.
     *
     * @param sender The message recipient
     * @param error  The error message
     */
    protected void sendError(@NotNull CommandSender sender, @NotNull String error) {
        sendMessage(sender, "§c" + error);
    }

    /**
     * Utility method to send a success message with standard formatting.
     *
     * @param sender  The message recipient
     * @param success The success message
     */
    protected void sendSuccess(@NotNull CommandSender sender, @NotNull String success) {
        sendMessage(sender, "§a" + success);
    }

    /**
     * Utility method to send a warning message with standard formatting.
     *
     * @param sender  The message recipient
     * @param warning The warning message
     */
    protected void sendWarning(@NotNull CommandSender sender, @NotNull String warning) {
        sendMessage(sender, "§e" + warning);
    }

    /**
     * Utility method to send the usage/syntax message.
     *
     * @param sender The message recipient
     */
    protected void sendUsage(@NotNull CommandSender sender) {
        sendMessage(sender, "§eUsage: §f" + getSyntax());
    }
}