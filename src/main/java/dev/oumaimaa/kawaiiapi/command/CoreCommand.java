package dev.oumaimaa.kawaiiapi.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a main command that contains subcommands within it.
 * Example: /stafftools freeze where "stafftools" is the core command and "freeze" is a subcommand.
 *
 * <p>This class handles:
 * <ul>
 *   <li>Subcommand routing and execution</li>
 *   <li>Tab completion for subcommands and their arguments</li>
 *   <li>Default command listing when no subcommand is provided</li>
 *   <li>Permission checking integration</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
final class CoreCommand extends Command {

    private final ArrayList<SubCommand> subcommands;
    private final CommandList commandList;

    /**
     * Constructs a new CoreCommand instance.
     *
     * @param name         The command name
     * @param description  Command description
     * @param usageMessage Usage message for the command
     * @param commandList  Custom command list display handler (can be null)
     * @param aliases      List of command aliases
     * @param subCommands  List of subcommands to register
     */
    CoreCommand(@NotNull String name,
                @NotNull String description,
                @NotNull String usageMessage,
                CommandList commandList,
                @NotNull List<String> aliases,
                @NotNull ArrayList<SubCommand> subCommands) {
        super(name, description, usageMessage, aliases);
        this.subcommands = subCommands;
        this.commandList = commandList;
    }

    /**
     * Gets all registered subcommands for this core command.
     *
     * @return ArrayList of SubCommand instances
     */
    @NotNull
    ArrayList<SubCommand> getSubCommands() {
        return subcommands;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull String commandLabel,
                           @NotNull String @NotNull [] args) {

        if (args.length > 0) {
            String subCommandName = args[0].toLowerCase();

            // Find and execute matching subcommand
            SubCommand matchedCommand = findSubCommand(subCommandName);

            if (matchedCommand != null) {
                // Check permission if set
                String permission = matchedCommand.getPermission();
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    Component noPermMsg = Component.text("You don't have permission to use this command!", NamedTextColor.RED);
                    sender.sendMessage(noPermMsg);
                    return true;
                }

                // Execute the subcommand
                try {
                    matchedCommand.perform(sender, args);
                } catch (Exception e) {
                    Component errorMsg = Component.text("An error occurred while executing this command!", NamedTextColor.RED);
                    sender.sendMessage(errorMsg);
                    e.printStackTrace();
                }
                return true;
            }

            // Subcommand not found
            Component notFoundMsg = Component.text()
                    .append(Component.text("Unknown subcommand: ", NamedTextColor.RED))
                    .append(Component.text(args[0], NamedTextColor.YELLOW))
                    .append(Component.text(". Use ", NamedTextColor.RED))
                    .append(Component.text("/" + getName(), NamedTextColor.GOLD))
                    .append(Component.text(" to see available commands.", NamedTextColor.RED))
                    .build();
            sender.sendMessage(notFoundMsg);
        } else {
            displayCommandList(sender);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                             @NotNull String alias,
                                             @NotNull String @NotNull [] args)
            throws IllegalArgumentException {

        if (args.length == 1) {
            // Tab complete subcommand names
            String partial = args[0].toLowerCase();
            return getSubCommands().stream()
                    .filter(subCommand -> {
                        // Check permission for tab completion
                        String permission = subCommand.getPermission();
                        return (permission == null || permission.isEmpty() || sender.hasPermission(permission));
                    })
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length >= 2) {
            // Tab complete subcommand arguments
            String subCommandName = args[0].toLowerCase();
            SubCommand matchedCommand = findSubCommand(subCommandName);

            if (matchedCommand != null) {
                // Check permission
                String permission = matchedCommand.getPermission();
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    return Collections.emptyList();
                }

                List<String> subCommandArgs = matchedCommand.getSubcommandArguments(sender, args);

                if (subCommandArgs != null && !subCommandArgs.isEmpty()) {
                    String partial = args[args.length - 1].toLowerCase();
                    return subCommandArgs.stream()
                            .filter(arg -> arg.toLowerCase().startsWith(partial))
                            .sorted()
                            .collect(Collectors.toList());
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Finds a subcommand by name or alias.
     *
     * @param name The subcommand name or alias to search for
     * @return The matching SubCommand, or null if not found
     */
    private @Nullable SubCommand findSubCommand(@NotNull String name) {
        String lowerName = name.toLowerCase();

        for (SubCommand subCommand : subcommands) {
            // Check exact name match
            if (subCommand.getName().equalsIgnoreCase(lowerName)) {
                return subCommand;
            }

            // Check aliases
            List<String> aliases = subCommand.getAliases();
            if (aliases != null && aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(lowerName))) {
                return subCommand;
            }
        }

        return null;
    }

    /**
     * Displays the command list to the sender using either custom or default formatting.
     *
     * @param sender The entity requesting the command list
     */
    private void displayCommandList(@NotNull CommandSender sender) {
        if (commandList == null) {
            displayDefaultCommandList(sender);
        } else {
            commandList.displayCommandList(sender, subcommands);
        }
    }

    /**
     * Displays the default command list with modern formatting.
     *
     * @param sender The command sender
     */
    private void displayDefaultCommandList(@NotNull CommandSender sender) {
        Component header = Component.text()
                .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY))
                .build();

        Component title = Component.text()
                .append(Component.text("Available Commands", NamedTextColor.GOLD, TextDecoration.BOLD))
                .build();

        sender.sendMessage(header);
        sender.sendMessage(title);
        sender.sendMessage(Component.empty());

        // Filter subcommands by permission
        List<SubCommand> availableCommands = subcommands.stream()
                .filter(subCommand -> {
                    String permission = subCommand.getPermission();
                    return permission == null || permission.isEmpty() || sender.hasPermission(permission);
                })
                .collect(Collectors.toList());

        if (availableCommands.isEmpty()) {
            Component noCommands = Component.text("No commands available.", NamedTextColor.RED);
            sender.sendMessage(noCommands);
        } else {
            for (SubCommand subcommand : availableCommands) {
                Component commandLine = Component.text()
                        .append(Component.text(subcommand.getSyntax(), NamedTextColor.YELLOW))
                        .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(subcommand.getDescription(), NamedTextColor.WHITE))
                        .build();
                sender.sendMessage(commandLine);
            }
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(header);
    }

    /**
     * Gets the number of registered subcommands.
     *
     * @return The subcommand count
     */
    int getSubCommandCount() {
        return subcommands.size();
    }

    /**
     * Checks if a subcommand with the given name exists.
     *
     * @param name The subcommand name to check
     * @return true if the subcommand exists
     */
    boolean hasSubCommand(@NotNull String name) {
        return findSubCommand(name) != null;
    }
}