package dev.oumaimaa.kawaiiapi.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced configuration management system using Paper's native YAML implementation.
 * Provides automatic serialization/deserialization with intelligent caching.
 *
 * <p>Features:
 * <ul>
 *   <li>Annotation-based configuration</li>
 *   <li>Automatic backup creation</li>
 *   <li>Configuration validation</li>
 *   <li>Hot-reloading support</li>
 *   <li>Thread-safe caching</li>
 *   <li>No external dependencies</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 2.0
 */
public final class ConfigManager {

    private static final ConcurrentHashMap<Class<?>, Object> configCache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Private constructor to prevent instantiation
    private ConfigManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Loads a configuration from file, or creates it with default values if it doesn't exist.
     * Results are cached for improved performance on subsequent calls.
     *
     * @param plugin      The plugin instance
     * @param configClass The configuration class annotated with @Config
     * @param <T>         The type of the configuration class
     * @return The loaded or newly created configuration instance
     * @throws IllegalStateException if the configuration cannot be loaded
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T loadConfig(@NotNull JavaPlugin plugin, @NotNull Class<T> configClass) {
        // Check cache first
        T cached = (T) configCache.get(configClass);
        if (cached != null) {
            return cached;
        }

        Config configAnnotation = configClass.getAnnotation(Config.class);

        if (configAnnotation == null) {
            String error = String.format(
                    "Configuration class '%s' is not annotated with @Config",
                    configClass.getSimpleName()
            );
            plugin.getSLF4JLogger().error(error);
            throw new IllegalStateException(error);
        }

        String fileName = configAnnotation.fileName();
        boolean createBackup = configAnnotation.createBackup();

        File configFile = getConfigFile(plugin, fileName);

        T config;

        try {
            if (!configFile.exists()) {
                // Create new config with defaults
                plugin.getSLF4JLogger().info("Creating new configuration file: {}", fileName);
                config = createDefaultConfig(configClass);
                saveConfigToFile(plugin, config, configFile);
            } else {
                // Load existing config
                plugin.getSLF4JLogger().info("Loading configuration file: {}", fileName);

                // Create backup if enabled
                if (createBackup) {
                    createBackup(plugin, configFile);
                }

                YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
                config = deserializeConfig(configClass, yamlConfig);

                // Save back to update any new fields
                saveConfigToFile(plugin, config, configFile);
            }

            // Cache the config
            configCache.put(configClass, config);

            return config;

        } catch (Exception e) {
            String error = String.format("Failed to load configuration '%s'", fileName);
            plugin.getSLF4JLogger().error(error, e);
            throw new IllegalStateException(error, e);
        }
    }

    /**
     * Saves a configuration instance to its file.
     * Automatically creates a backup of the existing file if configured.
     *
     * @param plugin       The plugin instance
     * @param configObject The configuration instance to save
     * @throws IllegalStateException if the configuration cannot be saved
     */
    public static void saveConfig(@NotNull JavaPlugin plugin, @NotNull Object configObject) {
        Config configAnnotation = configObject.getClass().getAnnotation(Config.class);

        if (configAnnotation == null) {
            String error = String.format(
                    "Configuration class '%s' is not annotated with @Config",
                    configObject.getClass().getSimpleName()
            );
            plugin.getSLF4JLogger().error(error);
            throw new IllegalStateException(error);
        }

        String fileName = configAnnotation.fileName();
        boolean createBackup = configAnnotation.createBackup();

        plugin.getSLF4JLogger().info("Saving configuration file: {}", fileName);

        File configFile = getConfigFile(plugin, fileName);

        // Create backup before saving
        if (createBackup && configFile.exists()) {
            createBackup(plugin, configFile);
        }

        try {
            saveConfigToFile(plugin, configObject, configFile);
            // Update cache
            configCache.put(configObject.getClass(), configObject);
        } catch (Exception e) {
            String error = String.format("Failed to save configuration '%s'", fileName);
            plugin.getSLF4JLogger().error(error, e);
            throw new IllegalStateException(error, e);
        }
    }

    /**
     * Reloads a configuration from file, bypassing the cache.
     *
     * @param plugin      The plugin instance
     * @param configClass The configuration class
     * @param <T>         The type of the configuration class
     * @return The reloaded configuration instance
     */
    @NotNull
    public static <T> T reloadConfig(@NotNull JavaPlugin plugin, @NotNull Class<T> configClass) {
        configCache.remove(configClass);
        return loadConfig(plugin, configClass);
    }

    /**
     * Clears the configuration cache for a specific class.
     *
     * @param configClass The configuration class to clear from cache
     */
    public static void clearCache(@NotNull Class<?> configClass) {
        configCache.remove(configClass);
    }

    /**
     * Clears all cached configurations.
     */
    public static void clearAllCaches() {
        configCache.clear();
    }

    /**
     * Gets the configuration file for the specified name.
     *
     * @param plugin   The plugin instance
     * @param fileName The base file name (without extension)
     * @return The configuration File object
     */
    @NotNull
    private static File getConfigFile(@NotNull JavaPlugin plugin, @NotNull String fileName) {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        return new File(plugin.getDataFolder(), fileName + ".yml");
    }

    /**
     * Creates a default configuration instance using the no-args constructor.
     *
     * @param configClass The configuration class
     * @param <T>         The type of the configuration class
     * @return New configuration instance with default values
     */
    @NotNull
    private static <T> T createDefaultConfig(@NotNull Class<T> configClass) {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create default config for " + configClass.getSimpleName() +
                            ". Ensure it has a public no-args constructor.", e);
        }
    }

    /**
     * Saves a configuration object to a file using Bukkit's YAML system.
     *
     * @param plugin The plugin instance
     * @param config The configuration object
     * @param file   The target file
     * @throws IOException if saving fails
     */
    private static void saveConfigToFile(@NotNull JavaPlugin plugin,
                                         @NotNull Object config,
                                         @NotNull File file) throws IOException {
        YamlConfiguration yamlConfig = new YamlConfiguration();
        serializeConfig(config, yamlConfig);
        yamlConfig.save(file);
        plugin.getSLF4JLogger().debug("Successfully saved configuration to: {}", file.getName());
    }

    /**
     * Serializes a configuration object into a YamlConfiguration.
     *
     * @param config     The configuration object
     * @param yamlConfig The target YamlConfiguration
     */
    private static void serializeConfig(@NotNull Object config, @NotNull YamlConfiguration yamlConfig) {
        Class<?> clazz = config.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            // Skip static and transient fields
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object value = field.get(config);
                String path = field.getName();

                if (value == null) {
                    yamlConfig.set(path, null);
                } else if (isSimpleType(value)) {
                    yamlConfig.set(path, value);
                } else if (value instanceof List) {
                    yamlConfig.set(path, value);
                } else if (value instanceof Map) {
                    yamlConfig.set(path, value);
                } else {
                    // Nested object - serialize recursively
                    ConfigurationSection section = yamlConfig.createSection(path);
                    serializeNestedObject(value, section);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to serialize field: " + field.getName(), e);
            }
        }
    }

    /**
     * Serializes a nested configuration object into a ConfigurationSection.
     *
     * @param obj     The nested object
     * @param section The target section
     */
    private static void serializeNestedObject(@NotNull Object obj, @NotNull ConfigurationSection section) {
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object value = field.get(obj);
                String path = field.getName();

                if (value == null) {
                    section.set(path, null);
                } else if (isSimpleType(value)) {
                    section.set(path, value);
                } else if (value instanceof List) {
                    section.set(path, value);
                } else if (value instanceof Map) {
                    section.set(path, value);
                } else {
                    // Further nested object
                    ConfigurationSection nestedSection = section.createSection(path);
                    serializeNestedObject(value, nestedSection);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to serialize nested field: " + field.getName(), e);
            }
        }
    }

    /**
     * Deserializes a YamlConfiguration into a configuration object.
     *
     * @param configClass The configuration class
     * @param yamlConfig  The source YamlConfiguration
     * @param <T>         The type of the configuration class
     * @return The deserialized configuration object
     */
    @NotNull
    private static <T> T deserializeConfig(@NotNull Class<T> configClass,
                                           @NotNull YamlConfiguration yamlConfig) {
        T config = createDefaultConfig(configClass);

        for (Field field : configClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String path = field.getName();

            try {
                if (!yamlConfig.contains(path)) {
                    continue; // Keep default value
                }

                Object value = yamlConfig.get(path);

                if (value == null) {
                    field.set(config, null);
                } else if (isSimpleType(value)) {
                    field.set(config, value);
                } else if (value instanceof List) {
                    field.set(config, value);
                } else if (value instanceof Map) {
                    field.set(config, value);
                } else if (yamlConfig.isConfigurationSection(path)) {
                    // Nested object
                    ConfigurationSection section = yamlConfig.getConfigurationSection(path);
                    Object nestedObj = deserializeNestedObject(field.getType(), section);
                    field.set(config, nestedObj);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize field: " + field.getName(), e);
            }
        }

        return config;
    }

    /**
     * Deserializes a ConfigurationSection into a nested object.
     *
     * @param clazz   The class of the nested object
     * @param section The source section
     * @return The deserialized object
     */
    @NotNull
    private static Object deserializeNestedObject(@NotNull Class<?> clazz,
                                                  @NotNull ConfigurationSection section) {
        try {
            Object obj = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                String path = field.getName();

                if (!section.contains(path)) {
                    continue;
                }

                Object value = section.get(path);

                if (value == null) {
                    field.set(obj, null);
                } else if (isSimpleType(value)) {
                    field.set(obj, value);
                } else if (value instanceof List) {
                    field.set(obj, value);
                } else if (value instanceof Map) {
                    field.set(obj, value);
                } else if (section.isConfigurationSection(path)) {
                    ConfigurationSection nestedSection = section.getConfigurationSection(path);
                    Object nestedObj = deserializeNestedObject(field.getType(), nestedSection);
                    field.set(obj, nestedObj);
                }
            }

            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize nested object: " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Checks if a value is a simple type that can be directly serialized.
     *
     * @param value The value to check
     * @return true if it's a simple type
     */
    private static boolean isSimpleType(@NotNull Object value) {
        return value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof Character;
    }

    /**
     * Creates a timestamped backup of a configuration file.
     *
     * @param plugin     The plugin instance
     * @param configFile The file to back up
     */
    private static void createBackup(@NotNull JavaPlugin plugin, @NotNull File configFile) {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(BACKUP_FORMAT);
            String backupName = configFile.getName().replace(".yml", "_" + timestamp + ".yml");
            File backupFile = new File(backupDir, backupName);

            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getSLF4JLogger().debug("Created backup: {}", backupFile.getName());
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Failed to create backup for {}", configFile.getName(), e);
        }
    }

    /**
     * Enumeration of supported configuration file types.
     * Note: Only YAML is currently supported in this refactored version.
     */
    public enum FileType {
        /**
         * YAML format configuration (only supported format)
         */
        YAML
    }
}