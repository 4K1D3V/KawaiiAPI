package dev.oumaimaa.kawaiiapi.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Functional interface for customizing how subcommands are displayed to users.
 * Allows plugins to define their own command list formatting and presentation logic.
 *
 * <p>Example implementations might include:
 * <ul>
 *   <li>Paginated command lists</li>
 *   <li>Categorized commands by functionality</li>
 *   <li>Interactive command menus</li>
 *   <li>Permission-filtered displays</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
@FunctionalInterface
public interface CommandList {

    /**
     * Displays a list of subcommands to the command sender.
     * This method is called when a player executes the core command without any arguments.
     *
     * @param sender         The entity that executed the command
     * @param subCommandList A list of all available subcommands for this core command
     */
    void displayCommandList(@NotNull CommandSender sender, @NotNull List<SubCommand> subCommandList);
}