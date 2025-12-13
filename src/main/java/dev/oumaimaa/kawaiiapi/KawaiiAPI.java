package dev.oumaimaa.kawaiiapi;

import dev.oumaimaa.kawaiiapi.menu.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * KawaiiAPI - Modern, high-performance framework for Paper plugins.
 *
 * <p>This API provides comprehensive utilities for:
 * <ul>
 *   <li>Command management with subcommand support</li>
 *   <li>Menu systems (standard and paginated)</li>
 *   <li>Configuration management (JSON/YAML)</li>
 *   <li>Text formatting with Adventure components</li>
 *   <li>Custom player skulls without NMS</li>
 *   <li>Region management and visualization</li>
 *   <li>Chat input handling</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class KawaiiAPI extends JavaPlugin {

    private static KawaiiAPI instance;

    /**
     * Retrieves the singleton instance of KawaiiAPI.
     *
     * @return The plugin instance, or null if not loaded
     * @throws IllegalStateException if called before plugin initialization
     */
    public static KawaiiAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("KawaiiAPI has not been initialized yet!");
        }
        return instance;
    }

    /**
     * Checks if KawaiiAPI is currently loaded and enabled.
     *
     * @return true if the plugin is loaded and enabled
     */
    public static boolean isLoaded() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public void onEnable() {
        instance = this;

        try {
            MenuManager.setup(getServer(), this);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize MenuManager", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Component banner = Component.text()
                .append(Component.text("╔═══════════════════════════════════════╗", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("║ ", NamedTextColor.AQUA))
                .append(Component.text("KawaiiAPI v1.0", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" - Initialized     ║", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("║   Modern Paper Plugin Framework       ║", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("║   Compatible with MC 1.21.4+          ║", NamedTextColor.AQUA))
                .append(Component.newline())
                .append(Component.text("╚═══════════════════════════════════════╝", NamedTextColor.AQUA))
                .build();

        getSLF4JLogger().info(banner.toString());
    }

    @Override
    public void onDisable() {
        getSLF4JLogger().info("KawaiiAPI v1.0 disabled successfully");
        instance = null;
    }
}