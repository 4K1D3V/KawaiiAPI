package dev.oumaimaa.kawaiiapi.menu;

import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerException;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerNotSetupException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for handling menu interactions and lifecycle events.
 * Automatically registered by MenuManager during initialization.
 *
 * <p>This listener handles:
 * <ul>
 *   <li>Inventory click events for menu interactions</li>
 *   <li>Inventory close events for cleanup</li>
 *   <li>Player quit events to clear cached data</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
final class MenuListener implements Listener {

    /**
     * Handles click events in menu inventories.
     * Routes the event to the appropriate Menu's handleMenu method.
     * Automatically cancels clicks if the menu specifies cancelAllClicks().
     *
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMenuClick(@NotNull InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Check if the clicked inventory belongs to a Menu
        if (holder instanceof Menu menu) {

            // Prevent null item interactions
            if (event.getCurrentItem() == null) {
                return;
            }

            // Cancel clicks if the menu requires it
            if (menu.cancelAllClicks()) {
                event.setCancelled(true);
            }

            // Route the event to the menu's handler
            try {
                menu.handleMenu(event);
            } catch (MenuManagerNotSetupException e) {
                handleMenuError(event, "MenuManager not initialized", e);
            } catch (MenuManagerException e) {
                handleMenuError(event, "Menu error occurred", e);
            } catch (Exception e) {
                handleMenuError(event, "Unexpected error in menu", e);
            }
        }
    }

    /**
     * Handles inventory close events for menus.
     * Calls the menu's handleMenuClose method for custom cleanup logic.
     *
     * @param event The inventory close event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuClose(@NotNull InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof Menu menu) {
            try {
                menu.handleMenuClose();
            } catch (Exception e) {
                if (event.getPlayer() instanceof Player player) {
                    MenuManager.getPlugin().getSLF4JLogger().warn(
                            "Error in menu close handler for player {}: {}",
                            player.getName(),
                            e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Handles player quit events to clean up cached menu data.
     * Prevents memory leaks by removing PlayerMenuUtility when players leave.
     *
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove the player's cached menu utility
        if (MenuManager.hasPlayerMenuUtility(player)) {
            MenuManager.removePlayerMenuUtility(player);

            if (MenuManager.getPlugin() != null) {
                MenuManager.getPlugin().getSLF4JLogger().debug(
                        "Cleaned up menu data for player: {}",
                        player.getName()
                );
            }
        }
    }

    /**
     * Handles errors that occur during menu interactions.
     * Logs the error and notifies the player with a user-friendly message.
     *
     * @param event     The inventory click event
     * @param message   The error message to display
     * @param exception The exception that occurred
     */
    private void handleMenuError(@NotNull InventoryClickEvent event,
                                 @NotNull String message,
                                 @NotNull Exception exception) {
        if (event.getWhoClicked() instanceof Player player) {
            // Log the error
            if (MenuManager.getPlugin() != null) {
                MenuManager.getPlugin().getSLF4JLogger().error(
                        "{} for player {}: {}",
                        message,
                        player.getName(),
                        exception.getMessage(),
                        exception
                );
            }

            // Notify the player
            Component errorMessage = Component.text()
                    .append(Component.text("⚠ ", NamedTextColor.RED))
                    .append(Component.text("An error occurred with the menu. ", NamedTextColor.RED))
                    .append(Component.text("Please try again.", NamedTextColor.GRAY))
                    .build();

            player.sendMessage(errorMessage);

            // Close the inventory to prevent further issues
            player.closeInventory();
        }
    }
}