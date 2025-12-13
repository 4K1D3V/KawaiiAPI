package dev.oumaimaa.kawaiiapi.colors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modern utility class for translating color codes in strings to Minecraft-compatible formats.
 * Supports legacy color codes (&amp;), hex colors (&#RRGGBB), and MiniMessage format.
 *
 * <p>Uses Adventure API exclusively for maximum compatibility with Paper 1.21+.
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class ColorTranslator {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // Private constructor to prevent instantiation
    private ColorTranslator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Translates color codes in a string using legacy format (&amp;) and hex colors (&#RRGGBB).
     * Returns an Adventure Component for modern Paper usage.
     *
     * @param text The string containing color codes
     * @return A Component with colors and formatting applied
     */
    public static @NotNull Component translateToComponent(@NotNull String text) {
        String processed = processHexColors(text);
        return LEGACY_AMPERSAND.deserialize(processed).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Translates color codes and returns a legacy formatted string.
     * Useful for compatibility with older APIs that require String format.
     *
     * @param text The string containing color codes
     * @return A string with Minecraft color codes (§) applied
     */
    public static @NotNull String translateColorCodes(@NotNull String text) {
        Component component = translateToComponent(text);
        return LEGACY_SECTION.serialize(component);
    }

    /**
     * Translates MiniMessage format to Adventure Component.
     * Supports advanced formatting like gradients, hover events, click events, etc.
     *
     * @param text The MiniMessage formatted text
     * @return A Component with formatting applied
     */
    public static @NotNull Component translateMiniMessage(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Strips all color codes and formatting from a string.
     *
     * @param text The text to strip colors from
     * @return Plain text without any formatting
     */
    public static @NotNull String stripColors(@NotNull String text) {
        Component component = translateToComponent(text);
        return PLAIN.serialize(component);
    }

    /**
     * Converts a Component back to a legacy color code string.
     *
     * @param component The component to serialize
     * @return Legacy formatted string
     */
    public static @NotNull String componentToLegacy(@NotNull Component component) {
        return LEGACY_SECTION.serialize(component);
    }

    /**
     * Converts a Component to plain text without any formatting.
     *
     * @param component The component to serialize
     * @return Plain text representation
     */
    public static @NotNull String componentToPlain(@NotNull Component component) {
        return PLAIN.serialize(component);
    }

    /**
     * Converts MiniMessage format to legacy color codes.
     *
     * @param miniMessage The MiniMessage text
     * @return Legacy formatted string
     */
    public static @NotNull String miniMessageToLegacy(@NotNull String miniMessage) {
        Component component = translateMiniMessage(miniMessage);
        return componentToLegacy(component);
    }

    /**
     * Processes hex color codes in format &#RRGGBB and converts them to proper format.
     *
     * @param text The text containing hex color codes
     * @return The text with hex colors converted
     */
    private static @NotNull String processHexColors(@NotNull String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "§x" + formatHexForLegacy(hex));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Formats a hex color string for legacy Minecraft format.
     * Converts RRGGBB to §x§R§R§G§G§B§B format.
     *
     * @param hex The hex color string (6 characters)
     * @return The formatted hex string
     */
    private static @NotNull String formatHexForLegacy(@NotNull String hex) {
        StringBuilder result = new StringBuilder();
        for (char c : hex.toLowerCase().toCharArray()) {
            result.append("§").append(c);
        }
        return result.toString();
    }

    /**
     * Builder class for creating complex colored text using method chaining.
     */
    public static final class ColorBuilder {
        private final StringBuilder text = new StringBuilder();

        /**
         * Appends colored text using legacy color codes.
         *
         * @param coloredText Text with color codes
         * @return This builder instance
         */
        public ColorBuilder append(@NotNull String coloredText) {
            text.append(coloredText);
            return this;
        }

        /**
         * Appends text with a specific color code.
         *
         * @param color   The color code (without &amp;)
         * @param content The text content
         * @return This builder instance
         */
        public ColorBuilder append(char color, @NotNull String content) {
            text.append("&").append(color).append(content);
            return this;
        }

        /**
         * Appends text with a hex color.
         *
         * @param hexColor The hex color (e.g., "FF0000")
         * @param content  The text content
         * @return This builder instance
         */
        public ColorBuilder appendHex(@NotNull String hexColor, @NotNull String content) {
            text.append("&#").append(hexColor).append(content);
            return this;
        }

        /**
         * Appends a new line.
         *
         * @return This builder instance
         */
        public ColorBuilder newLine() {
            text.append("\n");
            return this;
        }

        /**
         * Builds and returns the final Component.
         *
         * @return The constructed Component
         */
        public @NotNull Component build() {
            return translateToComponent(text.toString());
        }

        /**
         * Builds and returns the final legacy formatted string.
         *
         * @return The constructed string
         */
        public @NotNull String buildLegacy() {
            return translateColorCodes(text.toString());
        }
    }
}