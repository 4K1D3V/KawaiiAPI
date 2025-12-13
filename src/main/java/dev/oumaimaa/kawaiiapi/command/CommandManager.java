package dev.oumaimaa.kawaiiapi.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced command management system for Paper plugins.
 * Provides reflection-based command registration with subcommand support,
 * tab completion, and permission handling.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic subcommand registration</li>
 *   <li>Smart tab completion</li>
 *   <li>Command caching for performance</li>
 *   <li>Error recovery and logging</li>
 *   <li>Thread-safe command storage</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class CommandManager {

    private static final ConcurrentHashMap<String, CoreCommand> registeredCommands = new ConcurrentHashMap<>();
    private static CommandMap commandMap;

    // Private constructor to prevent instantiation
    private CommandManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates and registers a core command with subcommands using modern Paper command system.
     * This method uses reflection to access the server's CommandMap for registration.
     *
     * @param plugin             The plugin instance that owns this command
     * @param commandName        The name of the command to register
     * @param commandDescription Description of the command's functionality
     * @param commandUsage       Usage syntax for the command (e.g., "/command <arg>")
     * @param commandList        Optional custom display handler for command listing
     * @param aliases            List of command aliases (can be empty)
     * @param subcommands        Class references to SubCommand implementations
     * @throws IllegalStateException if command registration fails
     */
    @SafeVarargs
    public static void createCoreCommand(@NotNull JavaPlugin plugin,
                                         @NotNull String commandName,
                                         @NotNull String commandDescription,
                                         @NotNull String commandUsage,
                                         @Nullable CommandList commandList,
                                         @NotNull List<String> aliases,
                                         @NotNull Class<? extends SubCommand>... subcommands) {

        validateParameters(plugin, commandName, subcommands);

        ArrayList<SubCommand> commandInstances = instantiateSubcommands(plugin, subcommands);

        try {
            CommandMap map = getCommandMap();
            CoreCommand coreCommand = new CoreCommand(
                    commandName,
                    commandDescription,
                    commandUsage,
                    commandList,
                    aliases,
                    commandInstances
            );

            // Register command with plugin namespace for clarity
            boolean registered = map.register(plugin.getName().toLowerCase(), coreCommand);

            if (registered) {
                registeredCommands.put(commandName.toLowerCase(), coreCommand);
                plugin.getSLF4JLogger().info("Successfully registered command: /{}", commandName);
            } else {
                plugin.getSLF4JLogger().warn("Command /{} may already be registered", commandName);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to register command: " + commandName, e);
        }
    }

    /**
     * Creates and registers a core command without aliases.
     * Convenience method that calls the full registration method with an empty alias list.
     *
     * @param plugin             The plugin instance that owns this command
     * @param commandName        The name of the command to register
     * @param commandDescription Description of the command
     * @param commandUsage       Usage syntax for the command
     * @param commandList        Optional custom display handler
     * @param subcommands        Class references to SubCommand implementations
     */
    @SafeVarargs
    public static void createCoreCommand(@NotNull JavaPlugin plugin,
                                         @NotNull String commandName,
                                         @NotNull String commandDescription,
                                         @NotNull String commandUsage,
                                         @Nullable CommandList commandList,
                                         @NotNull Class<? extends SubCommand>... subcommands) {
        createCoreCommand(plugin, commandName, commandDescription, commandUsage,
                commandList, Collections.emptyList(), subcommands);
    }

    /**
     * Unregisters a previously registered command.
     * Useful for plugin reloading or dynamic command management.
     *
     * @param commandName The name of the command to unregister
     * @return true if the command was found and unregistered
     */
    public static boolean unregisterCommand(@NotNull String commandName) {
        CoreCommand command = registeredCommands.remove(commandName.toLowerCase());
        if (command != null) {
            try {
                CommandMap map = getCommandMap();
                command.unregister(map);
                return true;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to unregister command: " + commandName, e);
            }
        }
        return false;
    }

    /**
     * Retrieves a registered core command by name.
     *
     * @param commandName The command name to look up
     * @return The CoreCommand instance, or null if not found
     */
    @Nullable
    public static CoreCommand getCommand(@NotNull String commandName) {
        return registeredCommands.get(commandName.toLowerCase());
    }

    /**
     * Checks if a command is currently registered.
     *
     * @param commandName The command name to check
     * @return true if the command is registered
     */
    public static boolean isCommandRegistered(@NotNull String commandName) {
        return registeredCommands.containsKey(commandName.toLowerCase());
    }

    /**
     * Gets all currently registered command names.
     *
     * @return Immutable list of registered command names
     */
    @NotNull
    public static @Unmodifiable List<String> getRegisteredCommands() {
        return List.copyOf(registeredCommands.keySet());
    }

    /**
     * Retrieves the server's CommandMap using reflection.
     * Caches the result for improved performance on subsequent calls.
     *
     * @return The server's CommandMap instance
     * @throws ReflectiveOperationException if the CommandMap cannot be accessed
     */
    @NotNull
    private static CommandMap getCommandMap() throws ReflectiveOperationException {
        if (commandMap == null) {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        }
        return commandMap;
    }

    /**
     * Instantiates all subcommand classes using their no-args constructors.
     *
     * @param plugin      The plugin instance for logging
     * @param subcommands Array of subcommand classes to instantiate
     * @return List of instantiated SubCommand instances
     */
    @NotNull
    private static ArrayList<SubCommand> instantiateSubcommands(@NotNull JavaPlugin plugin,
                                                                @NotNull Class<? extends SubCommand>[] subcommands) {
        return Arrays.stream(subcommands)
                .map(subcommandClass -> {
                    try {
                        Constructor<? extends SubCommand> constructor = subcommandClass.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } catch (Exception e) {
                        plugin.getSLF4JLogger().error("Failed to instantiate subcommand: {}",
                                subcommandClass.getSimpleName(), e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Validates the parameters passed to command registration.
     *
     * @param plugin      The plugin instance
     * @param commandName The command name
     * @param subcommands The subcommand classes
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private static void validateParameters(@NotNull JavaPlugin plugin,
                                           @NotNull String commandName,
                                           @NotNull Class<? extends SubCommand>[] subcommands) {
        if (commandName.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty");
        }

        if (commandName.contains(" ")) {
            throw new IllegalArgumentException("Command name cannot contain spaces");
        }

        if (subcommands.length == 0) {
            plugin.getSLF4JLogger().warn("Registering command '{}' with no subcommands", commandName);
        }
    }

    /**
     * Builder class for creating commands with a fluent API.
     */
    public static final class CommandBuilder {
        private final JavaPlugin plugin;
        private final List<Class<? extends SubCommand>> subcommands = new ArrayList<>();
        private String name;
        private String description = "";
        private String usage = "";
        private CommandList commandList;
        private List<String> aliases = Collections.emptyList();

        /**
         * Creates a new CommandBuilder.
         *
         * @param plugin The plugin instance
         */
        public CommandBuilder(@NotNull JavaPlugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Sets the command name.
         *
         * @param name The command name
         * @return This builder instance
         */
        public CommandBuilder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the command description.
         *
         * @param description The description
         * @return This builder instance
         */
        public CommandBuilder description(@NotNull String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the command usage.
         *
         * @param usage The usage string
         * @return This builder instance
         */
        public CommandBuilder usage(@NotNull String usage) {
            this.usage = usage;
            return this;
        }

        /**
         * Sets the command list display handler.
         *
         * @param commandList The command list handler
         * @return This builder instance
         */
        public CommandBuilder commandList(@Nullable CommandList commandList) {
            this.commandList = commandList;
            return this;
        }

        /**
         * Sets the command aliases.
         *
         * @param aliases The aliases
         * @return This builder instance
         */
        public CommandBuilder aliases(@NotNull String... aliases) {
            this.aliases = Arrays.asList(aliases);
            return this;
        }

        /**
         * Adds a subcommand class.
         *
         * @param subcommand The subcommand class
         * @return This builder instance
         */
        public CommandBuilder addSubcommand(@NotNull Class<? extends SubCommand> subcommand) {
            this.subcommands.add(subcommand);
            return this;
        }

        /**
         * Adds multiple subcommand classes.
         *
         * @param subcommands The subcommand classes
         * @return This builder instance
         */
        @SafeVarargs
        public final CommandBuilder addSubcommands(@NotNull Class<? extends SubCommand>... subcommands) {
            this.subcommands.addAll(Arrays.asList(subcommands));
            return this;
        }

        /**
         * Builds and registers the command.
         *
         * @throws IllegalStateException if the command name is not set
         */
        public void register() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Command name must be set");
            }

            @SuppressWarnings("unchecked")
            Class<? extends SubCommand>[] subcommandArray = subcommands.toArray(new Class[0]);

            createCoreCommand(plugin, name, description, usage, commandList, aliases, subcommandArray);
        }
    }
}