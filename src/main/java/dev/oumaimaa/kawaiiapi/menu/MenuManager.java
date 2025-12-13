package dev.oumaimaa.kawaiiapi.menu;

import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerException;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerNotSetupException;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central management system for handling player menus.
 * Provides menu registration, opening, and player-specific menu data storage.
 *
 * <p>This manager must be initialized during plugin startup using {@link #setup(Server, Plugin)}.
 * It automatically registers the MenuListener and manages PlayerMenuUtility instances.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic listener registration</li>
 *   <li>Thread-safe player menu data storage</li>
 *   <li>Menu history tracking</li>
 *   <li>Reflection-based menu instantiation</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class MenuManager {

    private static final Map<UUID, PlayerMenuUtility> playerMenuUtilityMap = new ConcurrentHashMap<>();
    private static boolean isSetup = false;
    private static Plugin pluginInstance;

    // Private constructor to prevent instantiation
    private MenuManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Initializes the MenuManager system.
     * Must be called during plugin startup (onEnable).
     * Automatically registers the MenuListener if not already registered.
     *
     * @param server The server instance (use getServer())
     * @param plugin The plugin instance (use 'this' from main class)
     * @throws IllegalStateException if already initialized
     */
    public static void setup(@NotNull Server server, @NotNull Plugin plugin) {
        if (isSetup) {
            plugin.getSLF4JLogger().warn("MenuManager is already initialized");
            return;
        }

        pluginInstance = plugin;
        registerMenuListener(server, plugin);
        isSetup = true;

        plugin.getSLF4JLogger().info("MenuManager initialized successfully");
    }

    /**
     * Opens a menu for a player by instantiating the specified menu class.
     * The menu class must have a constructor that accepts PlayerMenuUtility.
     *
     * @param menuClass The class of the menu to open
     * @param player    The player to open the menu for
     * @param <T>       The type of the menu class
     * @throws MenuManagerNotSetupException If setup() has not been called
     * @throws MenuManagerException         If the menu cannot be instantiated or opened
     */
    public static <T extends Menu> void openMenu(@NotNull Class<T> menuClass, @NotNull Player player)
            throws MenuManagerException, MenuManagerNotSetupException {

        if (!isSetup) {
            throw new MenuManagerNotSetupException();
        }

        try {
            PlayerMenuUtility pmu = getPlayerMenuUtility(player);
            Constructor<T> constructor = menuClass.getDeclaredConstructor(PlayerMenuUtility.class);
            constructor.setAccessible(true);

            T menu = constructor.newInstance(pmu);
            menu.open();

        } catch (NoSuchMethodException e) {
            String error = String.format(
                    "Menu class '%s' does not have a constructor accepting PlayerMenuUtility",
                    menuClass.getSimpleName()
            );
            throw new MenuManagerException(error, e);

        } catch (InstantiationException e) {
            String error = String.format(
                    "Failed to instantiate menu class '%s'. Is it abstract?",
                    menuClass.getSimpleName()
            );
            throw new MenuManagerException(error, e);

        } catch (IllegalAccessException e) {
            String error = String.format(
                    "Cannot access constructor of menu class '%s'",
                    menuClass.getSimpleName()
            );
            throw new MenuManagerException(error, e);

        } catch (InvocationTargetException e) {
            String error = String.format(
                    "Error occurred while constructing menu class '%s'",
                    menuClass.getSimpleName()
            );
            throw new MenuManagerException(error, e.getCause());
        }
    }

    /**
     * Retrieves or creates a PlayerMenuUtility for the specified player.
     * PlayerMenuUtility instances are cached per player for performance.
     *
     * @param player The player to get the utility for
     * @return The PlayerMenuUtility instance for this player
     * @throws MenuManagerNotSetupException If setup() has not been called
     */
    @NotNull
    public static PlayerMenuUtility getPlayerMenuUtility(@NotNull Player player)
            throws MenuManagerNotSetupException {

        if (!isSetup) {
            throw new MenuManagerNotSetupException();
        }

        return playerMenuUtilityMap.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new PlayerMenuUtility(player)
        );
    }

    /**
     * Removes a player's MenuUtility from the cache.
     * Useful for cleanup when a player leaves the server.
     *
     * @param player The player to remove
     * @return The removed PlayerMenuUtility, or null if not found
     */
    @NotNull
    public static PlayerMenuUtility removePlayerMenuUtility(@NotNull Player player) {
        return playerMenuUtilityMap.remove(player.getUniqueId());
    }

    /**
     * Checks if a player has an active PlayerMenuUtility.
     *
     * @param player The player to check
     * @return true if the player has a cached utility
     */
    public static boolean hasPlayerMenuUtility(@NotNull Player player) {
        return playerMenuUtilityMap.containsKey(player.getUniqueId());
    }

    /**
     * Clears all cached PlayerMenuUtility instances.
     * Useful for plugin reloads or cleanup.
     */
    public static void clearAllPlayerMenuUtilities() {
        playerMenuUtilityMap.clear();
    }

    /**
     * Gets the number of cached PlayerMenuUtility instances.
     *
     * @return The cache size
     */
    public static int getCachedPlayerCount() {
        return playerMenuUtilityMap.size();
    }

    /**
     * Checks if the MenuManager has been initialized.
     *
     * @return true if setup() has been called
     */
    public static boolean isInitialized() {
        return isSetup;
    }

    /**
     * Gets the plugin instance associated with this MenuManager.
     *
     * @return The plugin instance, or null if not initialized
     */
    public static Plugin getPlugin() {
        return pluginInstance;
    }

    /**
     * Registers the MenuListener if not already registered.
     * Prevents duplicate listener registration.
     *
     * @param server The server instance
     * @param plugin The plugin instance
     */
    private static void registerMenuListener(@NotNull Server server, @NotNull Plugin plugin) {
        // Check if MenuListener is already registered
        boolean isAlreadyRegistered = false;

        for (RegisteredListener listener : InventoryClickEvent.getHandlerList().getRegisteredListeners()) {
            if (listener.getListener() instanceof MenuListener) {
                isAlreadyRegistered = true;
                break;
            }
        }

        if (!isAlreadyRegistered) {
            server.getPluginManager().registerEvents(new MenuListener(), plugin);
            plugin.getSLF4JLogger().debug("MenuListener registered successfully");
        } else {
            plugin.getSLF4JLogger().debug("MenuListener already registered");
        }
    }

    /**
     * Resets the MenuManager state.
     * WARNING: This will clear all cached data. Use with caution.
     */
    public static void reset() {
        playerMenuUtilityMap.clear();
        isSetup = false;
        pluginInstance = null;
    }
}