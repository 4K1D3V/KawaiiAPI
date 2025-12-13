package dev.oumaimaa.kawaiiapi.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * Abstract base class for creating paginated menus with automatic navigation.
 * Provides built-in support for first/previous/next/last page buttons and
 * automatic item distribution across multiple pages.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic page navigation buttons</li>
 *   <li>Customizable border items</li>
 *   <li>Smart caching for performance</li>
 *   <li>Page information display</li>
 *   <li>28 items per page by default</li>
 * </ul>
 *
 * <p>The layout reserves slots 0-9, 17-18, 26-27, 35-36, and 44-53 for borders
 * and navigation, leaving 28 slots for content per page.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public abstract class PaginatedMenu extends Menu {

    protected List<Object> data;
    protected int page = 0;
    protected int maxItemsPerPage = 28;
    private List<ItemStack> cachedItems;

    /**
     * Constructs a new PaginatedMenu instance.
     *
     * @param playerMenuUtility The PlayerMenuUtility containing player-specific data
     */
    protected PaginatedMenu(@NotNull PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    /**
     * Converts data objects to ItemStacks for display in the menu.
     * This method is called to populate the menu pages.
     * Results are cached for performance.
     *
     * @return List of ItemStacks to be displayed across pages
     */
    @NotNull
    public abstract List<ItemStack> dataToItems();

    /**
     * Provides custom items to override the default menu border buttons.
     * Return null to use default border items.
     *
     * @return HashMap mapping slot numbers to custom ItemStacks, or null for defaults
     */
    @Nullable
    public abstract HashMap<Integer, ItemStack> getCustomMenuBorderItems();

    /**
     * Sets up the border and navigation buttons for the paginated menu.
     * Can be overridden to create a completely custom border layout.
     */
    protected void addMenuBorder() {
        // Navigation buttons at the bottom
        inventory.setItem(47, createNavigationButton(Material.OAK_BUTTON, "⏮ First Page",
                hasPreviousPage()));
        inventory.setItem(48, createNavigationButton(Material.OAK_BUTTON, "◀ Previous",
                hasPreviousPage()));
        inventory.setItem(49, createNavigationButton(Material.BARRIER, "✖ Close", false));
        inventory.setItem(50, createNavigationButton(Material.OAK_BUTTON, "▶ Next",
                hasNextPage()));
        inventory.setItem(51, createNavigationButton(Material.OAK_BUTTON, "⏭ Last Page",
                hasNextPage()));

        // Page information
        Component pageInfo = Component.text()
                .append(Component.text("Page Information", NamedTextColor.GOLD, TextDecoration.BOLD))
                .build();

        Component currentPage = Component.text()
                .append(Component.text("Current: ", NamedTextColor.GRAY))
                .append(Component.text(getCurrentPage(), NamedTextColor.YELLOW))
                .build();

        Component totalPages = Component.text()
                .append(Component.text("Total: ", NamedTextColor.GRAY))
                .append(Component.text(getTotalPages(), NamedTextColor.YELLOW))
                .build();

        inventory.setItem(53, makeItemWithComponents(Material.PAPER, pageInfo, currentPage, totalPages));

        // Top border (slots 0-9)
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        // Side borders
        int[] sideBorders = {17, 18, 26, 27, 35, 36};
        for (int slot : sideBorders) {
            inventory.setItem(slot, super.FILLER_GLASS);
        }

        // Bottom border (slots 44-53, excluding button slots)
        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        // Apply custom items if provided
        HashMap<Integer, ItemStack> customItems = getCustomMenuBorderItems();
        if (customItems != null && !customItems.isEmpty()) {
            customItems.forEach((slot, item) -> inventory.setItem(slot, item));
        }
    }

    /**
     * Creates a navigation button with appropriate styling.
     *
     * @param material The button material
     * @param text     The button text
     * @param disabled Whether the button should appear disabled
     * @return The navigation button ItemStack
     */
    @NotNull
    private ItemStack createNavigationButton(@NotNull Material material,
                                             @NotNull String text,
                                             boolean disabled) {
        NamedTextColor color = disabled ? NamedTextColor.GRAY : NamedTextColor.GREEN;
        Component name = Component.text(text, color);
        return makeItemWithComponents(material, name);
    }

    /**
     * Creates an item with Component-based name and lore.
     *
     * @param material The material
     * @param name     The display name
     * @param lore     The lore components
     * @return The created ItemStack
     */
    @NotNull
    private ItemStack makeItemWithComponents(@NotNull Material material,
                                             @NotNull Component name,
                                             @NotNull Component... lore) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore.length > 0) {
                meta.lore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Gets the cached items, or generates them if not cached.
     *
     * @return List of ItemStacks for the current menu state
     */
    @NotNull
    protected List<ItemStack> getItems() {
        if (cachedItems == null) {
            cachedItems = dataToItems();
        }
        return cachedItems;
    }

    /**
     * Clears the item cache, forcing a refresh on the next access.
     * Call this when the underlying data changes.
     */
    protected void invalidateCache() {
        cachedItems = null;
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        List<ItemStack> items = getItems();

        if (items.isEmpty()) {
            Component noItems = Component.text("No Items", NamedTextColor.RED, TextDecoration.BOLD);
            Component subText = Component.text("There are no items to display.", NamedTextColor.GRAY);
            inventory.setItem(22, makeItemWithComponents(Material.BARRIER, noItems, subText));
            return;
        }

        int slot = 10;
        int startIndex = maxItemsPerPage * page;
        int endIndex = Math.min(startIndex + maxItemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            // Skip to next row when reaching the right border
            if (slot % 9 == 8) {
                slot += 2;
            }

            // Safety check to prevent out of bounds
            if (slot >= 44) {
                break;
            }

            inventory.setItem(slot, items.get(i));
            slot++;
        }
    }

    /**
     * Navigates to the previous page.
     *
     * @return true if navigation was successful, false if already on first page
     */
    public boolean prevPage() {
        if (page == 0) {
            Component message = Component.text("You are already on the first page!", NamedTextColor.RED);
            assert player != null;
            player.sendMessage(message);
            return false;
        }
        page--;
        reloadItems();

        Component message = Component.text()
                .append(Component.text("Navigated to page ", NamedTextColor.GREEN))
                .append(Component.text(getCurrentPage(), NamedTextColor.YELLOW))
                .build();
        assert player != null;
        player.sendMessage(message);
        return true;
    }

    /**
     * Navigates to the next page.
     *
     * @return true if navigation was successful, false if already on last page
     */
    public boolean nextPage() {
        int lastPageNumber = (getItems().size() - 1) / maxItemsPerPage;

        if (page >= lastPageNumber) {
            Component message = Component.text("You are already on the last page!", NamedTextColor.RED);
            assert player != null;
            player.sendMessage(message);
            return false;
        }
        page++;
        reloadItems();

        Component message = Component.text()
                .append(Component.text("Navigated to page ", NamedTextColor.GREEN))
                .append(Component.text(getCurrentPage(), NamedTextColor.YELLOW))
                .build();
        assert player != null;
        player.sendMessage(message);
        return true;
    }

    /**
     * Navigates to the first page.
     *
     * @return true if navigation was successful, false if already on first page
     */
    public boolean firstPage() {
        if (page == 0) {
            Component message = Component.text("You are already on the first page!", NamedTextColor.RED);
            assert player != null;
            player.sendMessage(message);
            return false;
        }
        page = 0;
        reloadItems();

        Component message = Component.text("Navigated to first page", NamedTextColor.GREEN);
        assert player != null;
        player.sendMessage(message);
        return true;
    }

    /**
     * Navigates to the last page.
     *
     * @return true if navigation was successful, false if already on last page
     */
    public boolean lastPage() {
        int lastPageNum = (getItems().size() - 1) / maxItemsPerPage;
        if (page == lastPageNum) {
            Component message = Component.text("You are already on the last page!", NamedTextColor.RED);
            assert player != null;
            player.sendMessage(message);
            return false;
        }
        page = lastPageNum;
        reloadItems();

        Component message = Component.text("Navigated to last page", NamedTextColor.GREEN);
        assert player != null;
        player.sendMessage(message);
        return true;
    }

    /**
     * Gets the maximum number of items that can be displayed per page.
     *
     * @return The max items per page (default: 28)
     */
    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    /**
     * Sets the maximum items per page.
     * Call this before opening the menu to adjust pagination.
     *
     * @param maxItems The maximum items per page (must be positive)
     * @throws IllegalArgumentException if maxItems is not positive
     */
    protected void setMaxItemsPerPage(int maxItems) {
        if (maxItems <= 0) {
            throw new IllegalArgumentException("Max items per page must be positive, got: " + maxItems);
        }
        this.maxItemsPerPage = maxItems;
    }

    /**
     * Gets the current page number (1-indexed for display).
     *
     * @return The current page number
     */
    public int getCurrentPage() {
        return page + 1;
    }

    /**
     * Gets the total number of pages.
     *
     * @return The total page count
     */
    public int getTotalPages() {
        List<ItemStack> items = getItems();
        return items.isEmpty() ? 1 : ((items.size() - 1) / maxItemsPerPage) + 1;
    }

    @Override
    public void open() {
        invalidateCache();
        super.open();
    }

    /**
     * Refreshes the menu data and reloads items without closing the menu.
     * Call this when the underlying data has changed.
     */
    public void refreshData() {
        invalidateCache();
        reloadItems();
    }

    /**
     * Checks if the menu has a previous page available.
     *
     * @return true if there is a previous page
     */
    protected boolean hasPreviousPage() {
        return page <= 0;
    }

    /**
     * Checks if the menu has a next page available.
     *
     * @return true if there is a next page
     */
    protected boolean hasNextPage() {
        int lastPageNumber = (getItems().size() - 1) / maxItemsPerPage;
        return page >= lastPageNumber;
    }

    /**
     * Gets the slot offset for the current page.
     * Useful for custom pagination implementations.
     *
     * @return The starting index for the current page
     */
    protected int getPageOffset() {
        return maxItemsPerPage * page;
    }

    /**
     * Checks if the current page is empty.
     *
     * @return true if there are no items on the current page
     */
    protected boolean isCurrentPageEmpty() {
        return getItems().isEmpty() || getPageOffset() >= getItems().size();
    }
}