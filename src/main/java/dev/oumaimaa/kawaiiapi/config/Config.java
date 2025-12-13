package dev.oumaimaa.kawaiiapi.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a configuration file.
 * Must be placed on classes that will be used with ConfigManager.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @Config(fileName = "config", fileType = ConfigManager.FileType.YAML)
 * public class MyConfig {
 *     public String serverName = "My Server";
 *     public int maxPlayers = 100;
 *     public List<String> enabledWorlds = List.of("world", "world_nether");
 *
 *     // No-args constructor required
 *     public MyConfig() {}
 * }
 * }
 * </pre>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {

    /**
     * The base name of the configuration file (without extension).
     * The appropriate extension (.json or .yml) will be added automatically.
     *
     * @return The file name
     */
    String fileName();

    /**
     * The format/type of the configuration file.
     *
     * @return The file type (JSON or YAML)
     */
    ConfigManager.FileType fileType();

    /**
     * Whether to create backups when saving the configuration.
     * Backups are timestamped and stored in the backups/ folder.
     *
     * @return true to create backups, false otherwise
     */
    boolean createBackup() default false;
}