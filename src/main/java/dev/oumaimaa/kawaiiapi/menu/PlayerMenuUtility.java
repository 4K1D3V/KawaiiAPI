package dev.oumaimaa.kawaiiapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Companion class for storing player-specific menu data.
 * Each player has exactly one PlayerMenuUtility instance that persists across menu navigation's.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Menu history tracking for back navigation</li>
 *   <li>Arbitrary data storage per player</li>
 *   <li>Type-safe data retrieval</li>
 * </ul>
 *
 * <p>Data stored in this utility persists until the player disconnects or
 * the MenuManager is cleared.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class PlayerMenuUtility {

    private final UUID ownerUUID;
    private final Map<String, Object> dataMap = new HashMap<>();
    private final Stack<Menu> menuHistory = new Stack<>();

    /**
     * Constructs a new PlayerMenuUtility for the specified player.
     *
     * @param player The player this utility belongs to
     */
    PlayerMenuUtility(@NotNull Player player) {
        this.ownerUUID = player.getUniqueId();
    }

    /**
     * Gets the player who owns this utility.
     *
     * @return The player instance, or null if they are offline
     */
    @Nullable
    public Player getOwner() {
        return Bukkit.getPlayer(ownerUUID);
    }

    /**
     * Gets the UUID of the player who owns this utility.
     *
     * @return The player's UUID
     */
    @NotNull
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Stores arbitrary data associated with a string key.
     * Overwrites any existing data with the same key.
     *
     * @param identifier The key to store the data under
     * @param data       The data to store (can be any object)
     */
    public void setData(@NotNull String identifier, @Nullable Object data) {
        if (data == null) {
            dataMap.remove(identifier);
        } else {
            dataMap.put(identifier, data);
        }
    }

    /**
     * Stores arbitrary data associated with an enum key.
     * Useful for type-safe key management.
     *
     * @param identifier The enum key to store the data under
     * @param data       The data to store
     */
    public void setData(@NotNull Enum<?> identifier, @Nullable Object data) {
        setData(identifier.name(), data);
    }

    /**
     * Retrieves data stored under the specified key.
     *
     * @param identifier The key to retrieve data for
     * @return The stored data, or null if not found
     */
    @Nullable
    public Object getData(@NotNull String identifier) {
        return dataMap.get(identifier);
    }

    /**
     * Retrieves data stored under the specified enum key.
     *
     * @param identifier The enum key to retrieve data for
     * @return The stored data, or null if not found
     */
    @Nullable
    public Object getData(@NotNull Enum<?> identifier) {
        return getData(identifier.name());
    }

    /**
     * Retrieves data with automatic type casting.
     * Returns null if the data doesn't exist or cannot be cast to the specified type.
     *
     * @param identifier The key to retrieve data for
     * @param classRef   The class to cast the data to
     * @param <T>        The type to cast to
     * @return The cast data, or null if not found or cast failed
     */
    @Nullable
    public <T> T getData(@NotNull String identifier, @NotNull Class<T> classRef) {
        Object obj = dataMap.get(identifier);

        if (obj == null) {
            return null;
        }

        try {
            return classRef.cast(obj);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Retrieves data with automatic type casting using an enum key.
     *
     * @param identifier The enum key to retrieve data for
     * @param classRef   The class to cast the data to
     * @param <T>        The type to cast to
     * @return The cast data, or null if not found or cast failed
     */
    @Nullable
    public <T> T getData(@NotNull Enum<?> identifier, @NotNull Class<T> classRef) {
        return getData(identifier.name(), classRef);
    }

    /**
     * Checks if data exists for the specified key.
     *
     * @param identifier The key to check
     * @return true if data exists for this key
     */
    public boolean hasData(@NotNull String identifier) {
        return dataMap.containsKey(identifier);
    }

    /**
     * Checks if data exists for the specified enum key.
     *
     * @param identifier The enum key to check
     * @return true if data exists for this key
     */
    public boolean hasData(@NotNull Enum<?> identifier) {
        return hasData(identifier.name());
    }

    /**
     * Removes data associated with the specified key.
     *
     * @param identifier The key to remove
     * @return The removed data, or null if not found
     */
    @Nullable
    public Object removeData(@NotNull String identifier) {
        return dataMap.remove(identifier);
    }

    /**
     * Removes data associated with the specified enum key.
     *
     * @param identifier The enum key to remove
     * @return The removed data, or null if not found
     */
    @Nullable
    public Object removeData(@NotNull Enum<?> identifier) {
        return removeData(identifier.name());
    }

    /**
     * Clears all stored data.
     * Does not affect menu history.
     */
    public void clearData() {
        dataMap.clear();
    }

    /**
     * Gets the number of data entries stored.
     *
     * @return The data map size
     */
    public int getDataSize() {
        return dataMap.size();
    }

    /**
     * Gets the previous menu from the history stack.
     * This method removes the current menu and returns the previous one.
     *
     * @return The previous menu, or null if history is empty
     */
    @Nullable
    Menu lastMenu() {
        // Remove current menu
        if (!menuHistory.isEmpty()) {
            menuHistory.pop();
        }

        // Return previous menu
        return menuHistory.isEmpty() ? null : menuHistory.pop();
    }

    /**
     * Adds a menu to the history stack.
     * Called automatically when a menu is opened.
     *
     * @param menu The menu to add to history
     */
    void pushMenu(@NotNull Menu menu) {
        menuHistory.push(menu);
    }

    /**
     * Gets the current size of the menu history stack.
     *
     * @return The number of menus in history
     */
    public int getMenuHistorySize() {
        return menuHistory.size();
    }

    /**
     * Clears the menu history stack.
     * Useful when you want to prevent back navigation.
     */
    public void clearMenuHistory() {
        menuHistory.clear();
    }

    /**
     * Checks if there is a previous menu in the history.
     *
     * @return true if back navigation is possible
     */
    public boolean hasPreviousMenu() {
        return menuHistory.size() > 1; // Current menu + at least one previous
    }

    /**
     * Peeks at the previous menu without removing it from history.
     *
     * @return The previous menu, or null if none exists
     */
    @Nullable
    public Menu peekPreviousMenu() {
        if (menuHistory.size() < 2) {
            return null;
        }

        int previousIndex = menuHistory.size() - 2;
        return menuHistory.get(previousIndex);
    }

    /**
     * Gets all stored data keys.
     *
     * @return Set of all data keys
     */
    @NotNull
    public @UnmodifiableView Set<String> getDataKeys() {
        return Collections.unmodifiableSet(dataMap.keySet());
    }

    @Override
    public String toString() {
        return String.format(
                "PlayerMenuUtility{owner=%s, dataSize=%d, historySize=%d}",
                ownerUUID,
                dataMap.size(),
                menuHistory.size()
        );
    }
}