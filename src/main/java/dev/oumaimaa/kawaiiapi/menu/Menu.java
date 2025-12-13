package dev.oumaimaa.kawaiiapi.menu;

import dev.oumaimaa.kawaiiapi.colors.ColorTranslator;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerException;
import dev.oumaimaa.kawaiiapi.exceptions.MenuManagerNotSetupException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base class for creating custom inventory menus using modern Paper API.
 * Provides a comprehensive framework for menu creation with built-in utilities
 * for item creation and inventory management.
 *
 * <p>Features:
 * <ul>
 *   <li>Adventure Component support for all text</li>
 *   <li>Automatic filler glass placement</li>
 *   <li>Menu history and navigation</li>
 *   <li>Customizable click handling</li>
 *   <li>Close event handling</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public abstract class Menu implements InventoryHolder {

    protected final PlayerMenuUtility playerMenuUtility;
    protected final Player player;
    protected Inventory inventory;
    protected ItemStack FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");

    /**
     * Constructs a new Menu instance.
     *
     * @param playerMenuUtility The PlayerMenuUtility containing player-specific data
     */
    protected Menu(@NotNull PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
        this.player = playerMenuUtility.getOwner();
    }

    /**
     * Gets the name/title of this menu to be displayed in the inventory.
     * Supports legacy color codes (&amp;) and hex colors (&#RRGGBB).
     *
     * @return The menu name
     */
    @NotNull
    public abstract String getMenuName();

    /**
     * Gets the number of slots for this menu inventory.
     * Must be a multiple of 9, between 9 and 54.
     *
     * @return The slot count
     */
    public abstract int getSlots();

    /**
     * Determines whether all inventory clicks should be cancelled by default.
     * Return true for non-interactive menus, false for menus where players can take items.
     *
     * @return true to cancel all clicks, false to allow item interaction
     */
    public abstract boolean cancelAllClicks();

    /**
     * Handles click events within this menu.
     * Override this method to implement custom click behavior.
     *
     * @param e The InventoryClickEvent to handle
     * @throws MenuManagerNotSetupException If the MenuManager is not initialized
     * @throws MenuManagerException         If an error occurs during menu handling
     */
    public abstract void handleMenu(@NotNull InventoryClickEvent e)
            throws MenuManagerNotSetupException, MenuManagerException;

    /**
     * Sets up all items in the menu inventory.
     * Called when the menu is opened or reloaded.
     * Override this method to populate your menu with items.
     */
    public abstract void setMenuItems();

    /**
     * Opens this menu for the player.
     * Creates the inventory, populates items, and displays it to the player.
     */
    public void open() {
        Component title = ColorTranslator.translateToComponent(getMenuName());
        inventory = Bukkit.createInventory(this, getSlots(), title);

        this.setMenuItems();

        playerMenuUtility.getOwner().openInventory(inventory);
        playerMenuUtility.pushMenu(this);
    }

    /**
     * Navigates back to the previous menu in the history stack.
     *
     * @throws MenuManagerException         If menu navigation fails
     * @throws MenuManagerNotSetupException If the MenuManager is not initialized
     */
    public void back() throws MenuManagerException, MenuManagerNotSetupException {
        Menu previousMenu = playerMenuUtility.lastMenu();
        if (previousMenu != null) {
            MenuManager.openMenu(previousMenu.getClass(), playerMenuUtility.getOwner());
        }
    }

    /**
     * Reloads the current menu by clearing and repopulating all items.
     * Maintains the menu open while refreshing content.
     */
    protected void reloadItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
        setMenuItems();
    }

    /**
     * Closes and reopens this menu, effectively performing a full refresh.
     *
     * @throws MenuManagerException         If menu reload fails
     * @throws MenuManagerNotSetupException If the MenuManager is not initialized
     */
    protected void reload() throws MenuManagerException, MenuManagerNotSetupException {
        player.closeInventory();
        MenuManager.openMenu(this.getClass(), player);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Fills all empty slots in the inventory with the default filler glass.
     */
    public void setFillerGlass() {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }

    /**
     * Fills all empty slots with a custom filler item.
     *
     * @param itemStack The item to use as filler
     */
    public void setFillerGlass(@NotNull ItemStack itemStack) {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, itemStack);
            }
        }
    }

    /**
     * Creates an ItemStack with display name and lore using modern Components.
     * Automatically translates color codes using ColorTranslator.
     *
     * @param material    The material type
     * @param displayName The display name (supports &amp; and &#RRGGBB)
     * @param lore        The lore lines (supports &amp; and &#RRGGBB)
     * @return The constructed ItemStack
     */
    @NotNull
    protected ItemStack makeItem(@NotNull Material material,
                                 @NotNull String displayName,
                                 @NotNull String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            itemMeta.displayName(ColorTranslator.translateToComponent(displayName));

            if (lore.length > 0) {
                List<Component> loreComponents = Arrays.stream(lore)
                        .map(ColorTranslator::translateToComponent)
                        .collect(Collectors.toList());
                itemMeta.lore(loreComponents);
            }

            item.setItemMeta(itemMeta);
        }

        return item;
    }

    /**
     * Creates an ItemStack with a specific amount.
     *
     * @param material    The material type
     * @param amount      The stack size (1-64, automatically clamped)
     * @param displayName The display name
     * @param lore        The lore lines
     * @return The constructed ItemStack
     */
    @NotNull
    protected ItemStack makeItem(@NotNull Material material,
                                 int amount,
                                 @NotNull String displayName,
                                 @NotNull String... lore) {
        ItemStack item = makeItem(material, displayName, lore);
        item.setAmount(Math.clamp(amount, 1, item.getMaxStackSize()));
        return item;
    }

    /**
     * Called when a player closes this menu.
     * Override this method to handle menu closing events (cleanup, saving data, etc.).
     * Called before the inventory is actually closed.
     */
    public void handleMenuClose() {
        // Default empty implementation
        // Subclasses can override this for custom close behavior
    }

    /**
     * Gets the player who owns this menu instance.
     *
     * @return The player instance
     */
    @NotNull
    protected Player getPlayer() {
        return player;
    }

    /**
     * Gets the PlayerMenuUtility associated with this menu.
     *
     * @return The PlayerMenuUtility instance
     */
    @NotNull
    protected PlayerMenuUtility getPlayerMenuUtility() {
        return playerMenuUtility;
    }

    /**
     * Checks if the menu is currently open.
     *
     * @return true if the player has this menu open
     */
    protected boolean isOpen() {
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    /**
     * Sends a message to the player viewing this menu.
     *
     * @param message The message to send (supports color codes)
     */
    protected void sendMessage(@NotNull String message) {
        player.sendMessage(ColorTranslator.translateToComponent(message));
    }

    /**
     * Plays a sound to the player viewing this menu.
     *
     * @param sound  The sound to play
     * @param volume The volume (0.0-1.0)
     * @param pitch  The pitch (0.5-2.0)
     */
    protected void playSound(@NotNull Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}