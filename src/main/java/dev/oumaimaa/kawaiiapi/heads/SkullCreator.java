package dev.oumaimaa.kawaiiapi.heads;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;

/**
 * Modern library for creating custom player skulls using Paper's Profile API.
 * Provides non-NMS methods for setting skull textures from UUIDs, URLs, and base64 data.
 *
 * <p>This implementation is fully compatible with Paper 1.21+ and uses only stable APIs.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class SkullCreator {

    private static final String TEXTURES_PROPERTY = "textures";

    private SkullCreator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a player skull ItemStack from a player's UUID.
     *
     * @param uuid The player's UUID
     * @return ItemStack with the player's head texture
     */
    public static @NotNull ItemStack itemFromUuid(@NotNull UUID uuid) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        return itemWithUuid(item, uuid);
    }

    /**
     * Applies a player's texture to an existing skull ItemStack using their UUID.
     *
     * @param item The skull ItemStack to modify (must be PLAYER_HEAD)
     * @param uuid The player's UUID
     * @return The modified ItemStack
     * @throws IllegalArgumentException if item is not a player head
     */
    public static @NotNull ItemStack itemWithUuid(@NotNull ItemStack item, @NotNull UUID uuid) {
        validateSkullItem(item);
        validateNotNull(uuid, "uuid");

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Failed to get skull meta");
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Creates a player skull ItemStack from a Mojang texture URL.
     *
     * @param url The full Mojang texture URL
     * @return ItemStack with the custom texture
     */
    public static @NotNull ItemStack itemFromUrl(@NotNull String url) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        return itemWithUrl(item, url);
    }

    /**
     * Applies a texture from a Mojang URL to an existing skull ItemStack.
     *
     * @param item The skull ItemStack to modify (must be PLAYER_HEAD)
     * @param url  The full Mojang texture URL
     * @return The modified ItemStack
     * @throws IllegalArgumentException if URL is invalid or item is not a player head
     */
    public static @NotNull ItemStack itemWithUrl(@NotNull ItemStack item, @NotNull String url) {
        validateSkullItem(item);
        validateNotNull(url, "url");

        return itemWithBase64(item, urlToBase64(url));
    }

    /**
     * Creates a player skull ItemStack from a base64 encoded texture string.
     *
     * @param base64 The base64 encoded texture data
     * @return ItemStack with the custom texture
     */
    public static @NotNull ItemStack itemFromBase64(@NotNull String base64) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        return itemWithBase64(item, base64);
    }

    /**
     * Applies a base64 encoded texture to an existing skull ItemStack.
     * Uses Paper's modern PlayerProfile API for reliable texture application.
     *
     * @param item   The skull ItemStack to modify (must be PLAYER_HEAD)
     * @param base64 The base64 encoded texture data
     * @return The modified ItemStack
     * @throws IllegalArgumentException if item is not a player head
     */
    public static @NotNull ItemStack itemWithBase64(@NotNull ItemStack item, @NotNull String base64) {
        validateSkullItem(item);
        validateNotNull(base64, "base64");

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            throw new IllegalStateException("Failed to get skull meta");
        }

        // Create a unique profile ID based on the texture
        UUID profileId = UUID.nameUUIDFromBytes(base64.getBytes());
        PlayerProfile profile = Bukkit.createProfile(profileId);
        profile.getProperties().add(new ProfileProperty(TEXTURES_PROPERTY, base64));

        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Sets a block to a player skull with the specified UUID.
     *
     * @param block The block to convert to a skull
     * @param uuid  The player's UUID
     * @throws IllegalStateException if block cannot be converted to a skull
     */
    public static void blockWithUuid(@NotNull Block block, @NotNull UUID uuid) {
        validateNotNull(block, "block");
        validateNotNull(uuid, "uuid");

        block.setType(Material.PLAYER_HEAD);
        if (block.getState() instanceof Skull skull) {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            skull.update();
        } else {
            throw new IllegalStateException("Failed to convert block to skull");
        }
    }

    /**
     * Sets a block to a player skull with a texture from a Mojang URL.
     *
     * @param block The block to convert to a skull
     * @param url   The Mojang texture URL
     * @throws IllegalArgumentException if URL is invalid
     * @throws IllegalStateException    if block cannot be converted to a skull
     */
    public static void blockWithUrl(@NotNull Block block, @NotNull String url) {
        validateNotNull(block, "block");
        validateNotNull(url, "url");

        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets a block to a player skull with a base64 encoded texture.
     *
     * @param block  The block to convert to a skull
     * @param base64 The base64 encoded texture data
     * @throws IllegalStateException if block cannot be converted to a skull
     */
    public static void blockWithBase64(@NotNull Block block, @NotNull String base64) {
        validateNotNull(block, "block");
        validateNotNull(base64, "base64");

        block.setType(Material.PLAYER_HEAD);
        if (block.getState() instanceof Skull skull) {
            UUID profileId = UUID.nameUUIDFromBytes(base64.getBytes());
            PlayerProfile profile = Bukkit.createProfile(profileId);
            profile.getProperties().add(new ProfileProperty(TEXTURES_PROPERTY, base64));

            skull.setPlayerProfile(profile);
            skull.update();
        } else {
            throw new IllegalStateException("Failed to convert block to skull");
        }
    }

    /**
     * Converts a Mojang texture URL to base64 encoded texture data.
     *
     * @param url The texture URL
     * @return Base64 encoded texture string
     * @throws IllegalArgumentException if the URL is invalid
     */
    private static @NotNull String urlToBase64(@NotNull String url) {
        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid texture URL: " + url, e);
        }

        String jsonTexture = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(jsonTexture.getBytes());
    }

    /**
     * Validates that an object is not null.
     *
     * @param obj  The object to check
     * @param name The parameter name for error messages
     * @throws IllegalArgumentException if the object is null
     */
    private static void validateNotNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    /**
     * Validates that an ItemStack is a valid skull item.
     *
     * @param item The ItemStack to validate
     * @throws IllegalArgumentException if not a player head
     */
    private static void validateSkullItem(@NotNull ItemStack item) {
        validateNotNull(item, "item");
        if (item.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("Item must be a PLAYER_HEAD, was " + item.getType());
        }
    }
}