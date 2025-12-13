package dev.oumaimaa.kawaiiapi.input;

import dev.oumaimaa.kawaiiapi.colors.ColorTranslator;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Modern chat input system using AsyncChatEvent instead of deprecated Conversation API.
 * Provides type-safe input validation with support for text, numeric, boolean, and option-based inputs.
 *
 * <p>This system automatically handles:
 * <ul>
 *   <li>Input cancellation via configurable escape words</li>
 *   <li>Automatic timeout and cleanup</li>
 *   <li>Player disconnect handling</li>
 *   <li>Type-specific validation</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
public final class ChatInput {

    private static final Map<UUID, InputSession> activeSessions = new ConcurrentHashMap<>();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    /**
     * Requests text input from a player with custom validation.
     *
     * @param plugin       The plugin instance
     * @param player       The player to request input from
     * @param prompt       The prompt message to display
     * @param validator    Function to validate input (return true if valid)
     * @param errorMessage Function to generate error message for invalid input
     * @param onInput      Callback invoked with valid input
     * @param onCancel     Callback invoked on cancellation (can be null)
     */
    public static void requestInput(@NotNull Plugin plugin,
                                    @NotNull Player player,
                                    @NotNull String prompt,
                                    @NotNull Predicate<String> validator,
                                    @NotNull java.util.function.Function<String, String> errorMessage,
                                    @NotNull Consumer<String> onInput,
                                    @Nullable Runnable onCancel) {
        cancelInput(player); // Cancel any existing session

        Component promptComponent = ColorTranslator.translateToComponent(prompt);
        player.sendMessage(promptComponent);
        player.sendMessage(ColorTranslator.translateToComponent("&7Type &ccancel &7to abort."));

        InputSession session = new InputSession(plugin, player, validator, errorMessage, onInput, onCancel);
        activeSessions.put(player.getUniqueId(), session);
        session.start();
    }

    /**
     * Requests simple text input without validation.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param onInput Callback for the input
     */
    public static void requestInput(@NotNull Plugin plugin,
                                    @NotNull Player player,
                                    @NotNull String prompt,
                                    @NotNull Consumer<String> onInput) {
        requestInput(plugin, player, prompt, input -> true, input -> "&cInvalid input.", onInput, null);
    }

    /**
     * Requests integer input from a player.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param onInput Callback with the parsed integer
     */
    public static void requestIntegerInput(@NotNull Plugin plugin,
                                           @NotNull Player player,
                                           @NotNull String prompt,
                                           @NotNull Consumer<Integer> onInput) {
        requestInput(plugin, player, prompt,
                input -> {
                    try {
                        Integer.parseInt(input);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                input -> "&cPlease enter a valid integer number.",
                input -> onInput.accept(Integer.parseInt(input)),
                null
        );
    }

    /**
     * Requests a double/decimal input from a player.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param onInput Callback with the parsed double
     */
    public static void requestNumericInput(@NotNull Plugin plugin,
                                           @NotNull Player player,
                                           @NotNull String prompt,
                                           @NotNull Consumer<Double> onInput) {
        requestInput(plugin, player, prompt,
                input -> {
                    try {
                        Double.parseDouble(input);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                input -> "&cPlease enter a valid number.",
                input -> onInput.accept(Double.parseDouble(input)),
                null
        );
    }

    /**
     * Requests an integer within a specific range.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param min     Minimum value (inclusive)
     * @param max     Maximum value (inclusive)
     * @param onInput Callback with the validated integer
     */
    public static void requestRangedIntegerInput(@NotNull Plugin plugin,
                                                 @NotNull Player player,
                                                 @NotNull String prompt,
                                                 int min,
                                                 int max,
                                                 @NotNull Consumer<Integer> onInput) {
        requestInput(plugin, player,
                prompt + " &7(&e" + min + "&7-&e" + max + "&7)",
                input -> {
                    try {
                        int value = Integer.parseInt(input);
                        return value >= min && value <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                input -> "&cPlease enter a number between " + min + " and " + max + ".",
                input -> onInput.accept(Integer.parseInt(input)),
                null
        );
    }

    /**
     * Requests boolean input (yes/no) from a player.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param onInput Callback with the boolean result
     */
    public static void requestBooleanInput(@NotNull Plugin plugin,
                                           @NotNull Player player,
                                           @NotNull String prompt,
                                           @NotNull Consumer<Boolean> onInput) {
        requestInput(plugin, player,
                prompt + " &7(&ayes&7/&cno&7)",
                input -> {
                    String lower = input.toLowerCase();
                    return lower.equals("yes") || lower.equals("no") ||
                            lower.equals("y") || lower.equals("n") ||
                            lower.equals("true") || lower.equals("false");
                },
                input -> "&cPlease answer with yes or no.",
                input -> {
                    String lower = input.toLowerCase();
                    boolean result = lower.equals("yes") || lower.equals("y") || lower.equals("true");
                    onInput.accept(result);
                },
                null
        );
    }

    /**
     * Requests input that must match one of the provided options.
     *
     * @param plugin  The plugin instance
     * @param player  The player to request input from
     * @param prompt  The prompt message
     * @param options Valid options (case-insensitive)
     * @param onInput Callback with the validated input
     */
    public static void requestOptionInput(@NotNull Plugin plugin,
                                          @NotNull Player player,
                                          @NotNull String prompt,
                                          @NotNull String[] options,
                                          @NotNull Consumer<String> onInput) {
        String optionsStr = String.join("&7/&e", options);
        requestInput(plugin, player,
                prompt + " &7(&e" + optionsStr + "&7)",
                input -> {
                    for (String option : options) {
                        if (option.equalsIgnoreCase(input)) {
                            return true;
                        }
                    }
                    return false;
                },
                input -> "&cPlease choose from: " + String.join(", ", options),
                onInput,
                null
        );
    }

    /**
     * Cancels any active input session for the specified player.
     *
     * @param player The player whose session should be cancelled
     * @return true if a session was cancelled, false otherwise
     */
    public static boolean cancelInput(@NotNull Player player) {
        InputSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.cancel();
            return true;
        }
        return false;
    }

    /**
     * Checks if a player has an active input session.
     *
     * @param player The player to check
     * @return true if the player has an active session
     */
    public static boolean hasActiveSession(@NotNull Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Internal class representing an active input session.
     */
    private static final class InputSession implements Listener {
        private static final long DEFAULT_TIMEOUT = 60 * 20L; // 60 seconds
        private final Plugin plugin;
        private final Player player;
        private final Predicate<String> validator;
        private final java.util.function.Function<String, String> errorMessage;
        private final Consumer<String> onInput;
        private final Runnable onCancel;
        private BukkitTask timeoutTask;

        InputSession(Plugin plugin, Player player, Predicate<String> validator,
                     java.util.function.Function<String, String> errorMessage,
                     Consumer<String> onInput, Runnable onCancel) {
            this.plugin = plugin;
            this.player = player;
            this.validator = validator;
            this.errorMessage = errorMessage;
            this.onInput = onInput;
            this.onCancel = onCancel;
        }

        void start() {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            // Set up timeout
            timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Component timeoutMsg = ColorTranslator.translateToComponent("&cInput timed out.");
                player.sendMessage(timeoutMsg);
                complete(false);
            }, DEFAULT_TIMEOUT);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(@NotNull AsyncChatEvent event) {
            if (!event.getPlayer().equals(player)) {
                return;
            }

            event.setCancelled(true);

            String input = PLAIN_SERIALIZER.serialize(event.message());

            // Check for cancellation
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit") ||
                    input.equalsIgnoreCase("quit")) {
                Component cancelMsg = ColorTranslator.translateToComponent("&cInput cancelled.");
                player.sendMessage(cancelMsg);
                complete(false);
                return;
            }

            // Validate input
            if (validator.test(input)) {
                // Run callback on main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        onInput.accept(input);
                    } catch (Exception e) {
                        plugin.getSLF4JLogger().error("Error processing chat input", e);
                        Component errorMsg = ColorTranslator.translateToComponent("&cAn error occurred processing your input.");
                        player.sendMessage(errorMsg);
                    }
                });
                complete(true);
            } else {
                Component error = ColorTranslator.translateToComponent(errorMessage.apply(input));
                player.sendMessage(error);
            }
        }

        @EventHandler
        public void onQuit(@NotNull PlayerQuitEvent event) {
            if (event.getPlayer().equals(player)) {
                complete(false);
            }
        }

        void cancel() {
            complete(false);
        }

        private void complete(boolean success) {
            HandlerList.unregisterAll(this);
            activeSessions.remove(player.getUniqueId());

            if (timeoutTask != null) {
                timeoutTask.cancel();
            }

            if (!success && onCancel != null) {
                plugin.getServer().getScheduler().runTask(plugin, onCancel);
            }
        }
    }
}