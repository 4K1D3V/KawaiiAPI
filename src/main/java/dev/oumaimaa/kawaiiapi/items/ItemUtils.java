package dev.oumaimaa.kawaiiapi.items;

import dev.oumaimaa.kawaiiapi.colors.ColorTranslator;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern utility class for creating and manipulating Minecraft items using Paper's Adventure API.
 * Provides builder-style methods for creating items with custom properties without deprecated methods.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class ItemUtils {

    // Private constructor to prevent instantiation
    private ItemUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates an ItemStack with display name and lore using modern Component API.
     * Supports legacy color codes (&amp;) and hex colors (&#RRGGBB).
     *
     * @param material    The material type for the item
     * @param displayName The display name (supports color codes)
     * @param lore        The lore lines (supports color codes)
     * @return The constructed ItemStack
     */
    @NotNull
    public static ItemStack makeItem(@NotNull Material material,
                                     @NotNull String displayName,
                                     @NotNull String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(ColorTranslator.translateToComponent(displayName));

            if (lore.length > 0) {
                List<Component> loreComponents = Arrays.stream(lore)
                        .map(ColorTranslator::translateToComponent)
                        .collect(Collectors.toList());
                meta.lore(loreComponents);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates an ItemStack with a specific amount.
     *
     * @param material    The material type for the item
     * @param amount      The stack size (1-64, clamped automatically)
     * @param displayName The display name (supports color codes)
     * @param lore        The lore lines (supports color codes)
     * @return The constructed ItemStack
     */
    @NotNull
    public static ItemStack makeItem(@NotNull Material material,
                                     int amount,
                                     @NotNull String displayName,
                                     @NotNull String... lore) {
        ItemStack item = makeItem(material, displayName, lore);
        item.setAmount(Math.clamp(amount, 1, item.getMaxStackSize()));
        return item;
    }

    /**
     * Creates an ItemStack using Adventure Components directly.
     * Provides full control over text styling without string parsing.
     *
     * @param material    The material type for the item
     * @param displayName The display name as Component
     * @param lore        The lore lines as Components
     * @return The constructed ItemStack
     */
    @NotNull
    public static ItemStack makeItemWithComponents(@NotNull Material material,
                                                   @NotNull Component displayName,
                                                   @NotNull Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(displayName);

            if (lore.length > 0) {
                meta.lore(Arrays.asList(lore));
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Adds an enchantment glow effect to an item without showing actual enchantments.
     * Uses UNBREAKABLE enchantment which is widely compatible.
     *
     * @param item The item to add glow to
     * @return The modified ItemStack
     */
    @NotNull
    public static ItemStack addGlow(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Makes an item unbreakable and hides the unbreakable tag from the tooltip.
     *
     * @param item The item to make unbreakable
     * @return The modified ItemStack
     */
    @NotNull
    public static ItemStack makeUnbreakable(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Hides all item flags (enchants, attributes, unbreakable, etc.).
     * Useful for creating clean-looking custom items.
     *
     * @param item The item to hide flags on
     * @return The modified ItemStack
     */
    @NotNull
    public static ItemStack hideAllFlags(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Sets custom model data on an item for resource pack support.
     *
     * @param item            The item to modify
     * @param customModelData The custom model data value
     * @return The modified ItemStack
     */
    @NotNull
    public static ItemStack setCustomModelData(@NotNull ItemStack item, int customModelData) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Sets the amount of an item, clamping to valid stack size.
     *
     * @param item   The item to modify
     * @param amount The desired amount
     * @return The modified ItemStack
     */
    @NotNull
    public static ItemStack setAmount(@NotNull ItemStack item, int amount) {
        item.setAmount(Math.clamp(amount, 1, item.getMaxStackSize()));
        return item;
    }

    /**
     * Checks if an item has a display name.
     *
     * @param item The item to check
     * @return true if the item has a custom display name
     */
    public static boolean hasDisplayName(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName();
    }

    /**
     * Checks if an item has lore.
     *
     * @param item The item to check
     * @return true if the item has lore
     */
    public static boolean hasLore(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasLore();
    }

    /**
     * Checks if an item has custom model data set.
     *
     * @param item The item to check
     * @return true if the item has custom model data
     */
    public static boolean hasCustomModelData(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData();
    }

    /**
     * Gets the display name of an item as a Component.
     *
     * @param item The item to get the name from
     * @return The display name Component, or null if not set
     */
    @Nullable
    public static Component getDisplayName(@Nullable ItemStack item) {
        if (!hasDisplayName(item)) {
            return null;
        }

        return item.getItemMeta().displayName();
    }

    /**
     * Gets the lore of an item as a list of Components.
     *
     * @param item The item to get lore from
     * @return The lore list, or null if not set
     */
    @Nullable
    public static List<Component> getLore(@Nullable ItemStack item) {
        if (!hasLore(item)) {
            return null;
        }

        return item.getItemMeta().lore();
    }

    /**
     * Builder class for creating complex items with a fluent API.
     * Provides method chaining for convenient item construction.
     */
    public static final class ItemBuilder {
        private final ItemStack item;
        private final ItemMeta meta;

        /**
         * Creates a new ItemBuilder.
         *
         * @param material The material for the item
         */
        public ItemBuilder(@NotNull Material material) {
            this.item = new ItemStack(material);
            this.meta = item.getItemMeta();
        }

        /**
         * Creates a new ItemBuilder from an existing item.
         *
         * @param item The item to base this builder on
         */
        public ItemBuilder(@NotNull ItemStack item) {
            this.item = item.clone();
            this.meta = this.item.getItemMeta();
        }

        /**
         * Sets the amount of the item.
         *
         * @param amount The amount (automatically clamped to valid range)
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder amount(int amount) {
            item.setAmount(Math.clamp(amount, 1, item.getMaxStackSize()));
            return this;
        }

        /**
         * Sets the display name using color codes.
         *
         * @param name The display name (supports &amp; and &#RRGGBB)
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder name(@NotNull String name) {
            if (meta != null) {
                meta.displayName(ColorTranslator.translateToComponent(name));
            }
            return this;
        }

        /**
         * Sets the display name using a Component.
         *
         * @param name The display name Component
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder name(@NotNull Component name) {
            if (meta != null) {
                meta.displayName(name);
            }
            return this;
        }

        /**
         * Sets the lore using color codes.
         *
         * @param lore The lore lines (supports &amp; and &#RRGGBB)
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder lore(@NotNull String... lore) {
            if (meta != null && lore.length > 0) {
                List<Component> loreComponents = Arrays.stream(lore)
                        .map(ColorTranslator::translateToComponent)
                        .collect(Collectors.toList());
                meta.lore(loreComponents);
            }
            return this;
        }

        /**
         * Sets the lore using Components.
         *
         * @param lore The lore Components
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder lore(@NotNull List<Component> lore) {
            if (meta != null) {
                meta.lore(lore);
            }
            return this;
        }

        /**
         * Adds lines to existing lore.
         *
         * @param lines The lines to add
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder addLore(@NotNull String... lines) {
            if (meta != null && lines.length > 0) {
                List<Component> currentLore = meta.lore();
                if (currentLore == null) {
                    return lore(lines);
                }

                Arrays.stream(lines)
                        .map(ColorTranslator::translateToComponent)
                        .forEach(currentLore::add);
                meta.lore(currentLore);
            }
            return this;
        }

        /**
         * Adds a glow effect.
         *
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder glow() {
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            return this;
        }

        /**
         * Makes the item unbreakable.
         *
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder unbreakable() {
            if (meta != null) {
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            return this;
        }

        /**
         * Sets custom model data.
         *
         * @param data The custom model data value
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder customModelData(int data) {
            if (meta != null) {
                meta.setCustomModelData(data);
            }
            return this;
        }

        /**
         * Hides all item flags.
         *
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder hideFlags() {
            if (meta != null) {
                meta.addItemFlags(ItemFlag.values());
            }
            return this;
        }

        /**
         * Hides specific item flags.
         *
         * @param flags The flags to hide
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder hideFlags(@NotNull ItemFlag... flags) {
            if (meta != null) {
                meta.addItemFlags(flags);
            }
            return this;
        }

        /**
         * Adds an enchantment to the item.
         *
         * @param enchantment The enchantment to add
         * @param level       The enchantment level
         * @return This builder instance
         */
        @NotNull
        public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
            if (meta != null) {
                meta.addEnchant(enchantment, level, true);
            }
            return this;
        }

        /**
         * Builds and returns the final ItemStack.
         *
         * @return The constructed ItemStack
         */
        @NotNull
        public ItemStack build() {
            if (meta != null) {
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}