package dev.oumaimaa.kawaiiapi.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced configuration management system supporting JSON and YAML formats.
 * Provides automatic serialization/deserialization using Jackson with intelligent caching.
 *
 * <p>Features:
 * <ul>
 *   <li>Annotation-based configuration</li>
 *   <li>Automatic backup creation</li>
 *   <li>Configuration validation</li>
 *   <li>Hot-reloading support</li>
 *   <li>Thread-safe caching</li>
 *   <li>Pretty-printed output</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
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
        FileType fileType = configAnnotation.fileType();
        boolean createBackup = configAnnotation.createBackup();

        File configFile = getConfigFile(plugin, fileName, fileType);
        ObjectMapper mapper = getObjectMapper(fileType);

        T config;

        try {
            if (!configFile.exists()) {
                // Create new config with defaults
                plugin.getSLF4JLogger().info("Creating new configuration file: {}", fileName);
                config = createDefaultConfig(configClass);
                saveConfigToFile(plugin, config, configFile, mapper);
            } else {
                // Load existing config
                plugin.getSLF4JLogger().info("Loading configuration file: {}", fileName);

                // Create backup if enabled
                if (createBackup) {
                    createBackup(plugin, configFile);
                }

                config = mapper.readValue(configFile, configClass);

                // Save back to update any new fields
                saveConfigToFile(plugin, config, configFile, mapper);
            }

            // Cache the config
            configCache.put(configClass, config);

            return config;

        } catch (IOException e) {
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
        FileType fileType = configAnnotation.fileType();
        boolean createBackup = configAnnotation.createBackup();

        plugin.getSLF4JLogger().info("Saving configuration file: {}", fileName);

        File configFile = getConfigFile(plugin, fileName, fileType);

        // Create backup before saving
        if (createBackup && configFile.exists()) {
            createBackup(plugin, configFile);
        }

        ObjectMapper mapper = getObjectMapper(fileType);

        try {
            saveConfigToFile(plugin, configObject, configFile, mapper);
            // Update cache
            configCache.put(configObject.getClass(), configObject);
        } catch (IOException e) {
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
     * Gets the configuration file for the specified name and type.
     *
     * @param plugin   The plugin instance
     * @param fileName The base file name (without extension)
     * @param fileType The file type (JSON or YAML)
     * @return The configuration File object
     */
    @NotNull
    private static File getConfigFile(@NotNull JavaPlugin plugin,
                                      @NotNull String fileName,
                                      @NotNull FileType fileType) {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        String extension = fileType == FileType.YAML ? ".yml" : ".json";
        return new File(plugin.getDataFolder(), fileName + extension);
    }

    /**
     * Gets a configured ObjectMapper for the specified file type.
     *
     * @param fileType The file type (JSON or YAML)
     * @return Configured ObjectMapper instance
     */
    @NotNull
    private static ObjectMapper getObjectMapper(@NotNull FileType fileType) {
        ObjectMapper mapper;

        if (fileType == FileType.YAML) {
            YAMLFactory yamlFactory = YAMLFactory.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
                    .build();
            mapper = new ObjectMapper(yamlFactory);
        } else {
            mapper = new ObjectMapper(new JsonFactory());
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            mapper.setDefaultPrettyPrinter(printer);
        }

        // Configure mapper
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
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
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Failed to create default config for " + configClass.getSimpleName() +
                            ". Ensure it has a public no-args constructor.", e);
        }
    }

    /**
     * Saves a configuration object to a file.
     *
     * @param plugin The plugin instance
     * @param config The configuration object
     * @param file   The target file
     * @param mapper The ObjectMapper to use
     * @throws IOException if saving fails
     */
    private static void saveConfigToFile(@NotNull JavaPlugin plugin,
                                         @NotNull Object config,
                                         @NotNull File file,
                                         @NotNull ObjectMapper mapper) throws IOException {
        mapper.writeValue(file, config);
        plugin.getSLF4JLogger().debug("Successfully saved configuration to: {}", file.getName());
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
            String backupName = configFile.getName().replace(".", "_" + timestamp + ".");
            File backupFile = new File(backupDir, backupName);

            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getSLF4JLogger().debug("Created backup: {}", backupFile.getName());
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Failed to create backup for {}", configFile.getName(), e);
        }
    }

    /**
     * Enumeration of supported configuration file types.
     */
    public enum FileType {
        /**
         * JSON format configuration
         */
        JSON,
        /**
         * YAML format configuration
         */
        YAML
    }
}