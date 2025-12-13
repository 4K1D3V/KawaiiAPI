package dev.oumaimaa.kawaiiapi.region;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Modern utility class for visualizing regions using glowing slime entities.
 * Provides methods to create, remove, and manage visual region selectors without deprecated APIs.
 *
 * <p>Features:
 * <ul>
 *   <li>Uses Adventure API NamedTextColor for modern color support</li>
 *   <li>Automatic team management for glow effects</li>
 *   <li>Tag-based entity tracking for cleanup</li>
 *   <li>Thread-safe team numbering</li>
 *   <li>Efficient entity spawning</li>
 * </ul>
 *
 * @author KawaiiDevelopment (Updated by milo)
 * @version 1.0
 */
public final class RegionSelector {

    private static final String TAG_PREFIX = "regionselector-";
    private static final String TEAM_PREFIX = "regionselector+";
    private static final AtomicInteger teamCounter = new AtomicInteger(0);

    // Private constructor to prevent instantiation
    private RegionSelector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Removes all selector entities with a specific tag.
     *
     * @param tag The identifier tag to remove selectors for
     */
    public static void killSelectorsWithTag(@NotNull String tag) {
        String fullTag = TAG_PREFIX + tag;

        for (World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(entity -> entity.getScoreboardTags().contains(fullTag))
                    .forEach(Entity::remove);
        }
    }

    /**
     * Removes all region selector entities across all worlds.
     */
    public static void killAllSelectors() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(entity -> entity.getScoreboardTags().stream()
                            .anyMatch(tag -> tag.startsWith(TAG_PREFIX)))
                    .forEach(Entity::remove);
        }
    }

    /**
     * Removes all temporary scoreboard teams created for region selectors.
     */
    public static void removeTempTeams() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        board.getTeams().stream()
                .filter(team -> team.getName().startsWith(TEAM_PREFIX))
                .forEach(Team::unregister);
    }

    /**
     * Draws a visual selector for the specified region using glowing slime entities.
     *
     * @param region    The region to visualize
     * @param id        Unique identifier for this selector
     * @param glowColor The color for the glowing effect (null for white)
     * @throws IllegalStateException if the region is not fully set
     */
    public static void drawSelector(@NotNull Region region,
                                    @NotNull String id,
                                    @Nullable NamedTextColor glowColor) {
        if (!region.isSet()) {
            throw new IllegalStateException("Region must be fully set before drawing selector");
        }

        Location loc1 = region.getCorner1();
        Location loc2 = region.getCorner2();
        World world = region.getWorld();

        if (world == null) {
            throw new IllegalStateException("Region world is null");
        }

        int minX = (int) Math.min(loc1.getX(), loc2.getX());
        int minY = (int) Math.min(loc1.getY(), loc2.getY());
        int minZ = (int) Math.min(loc1.getZ(), loc2.getZ());
        int maxX = (int) Math.max(loc1.getX(), loc2.getX());
        int maxY = (int) Math.max(loc1.getY(), loc2.getY());
        int maxZ = (int) Math.max(loc1.getZ(), loc2.getZ());

        Team selectorTeam = createSelectorTeam(glowColor);

        // Draw edges along X-axis (4 edges)
        for (int x = minX; x <= maxX; x++) {
            spawnMarker(world, selectorTeam, id, x, minY, minZ);
            spawnMarker(world, selectorTeam, id, x, maxY, minZ);
            spawnMarker(world, selectorTeam, id, x, minY, maxZ);
            spawnMarker(world, selectorTeam, id, x, maxY, maxZ);
        }

        // Draw edges along Y-axis (4 edges)
        for (int y = minY; y <= maxY; y++) {
            spawnMarker(world, selectorTeam, id, minX, y, minZ);
            spawnMarker(world, selectorTeam, id, maxX, y, minZ);
            spawnMarker(world, selectorTeam, id, minX, y, maxZ);
            spawnMarker(world, selectorTeam, id, maxX, y, maxZ);
        }

        // Draw edges along Z-axis (4 edges)
        for (int z = minZ; z <= maxZ; z++) {
            spawnMarker(world, selectorTeam, id, minX, minY, z);
            spawnMarker(world, selectorTeam, id, maxX, minY, z);
            spawnMarker(world, selectorTeam, id, minX, maxY, z);
            spawnMarker(world, selectorTeam, id, maxX, maxY, z);
        }
    }

    /**
     * Draws a selector with default white glow color.
     *
     * @param region The region to visualize
     * @param id     Unique identifier for this selector
     */
    public static void drawSelector(@NotNull Region region, @NotNull String id) {
        drawSelector(region, id, NamedTextColor.WHITE);
    }

    /**
     * Creates a scoreboard team for region selector entities.
     * Uses atomic counter for thread-safe team numbering.
     *
     * @param glowColor The glow color for the team (can be null)
     * @return The created team
     */
    @NotNull
    private static Team createSelectorTeam(@Nullable NamedTextColor glowColor) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        int teamNumber = teamCounter.incrementAndGet();
        String teamName = TEAM_PREFIX + teamNumber;

        Team selectorTeam = board.registerNewTeam(teamName);

        selectorTeam.color(Objects.requireNonNullElse(glowColor, NamedTextColor.WHITE));

        selectorTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        selectorTeam.setCanSeeFriendlyInvisibles(false);

        return selectorTeam;
    }

    /**
     * Spawns a single marker slime entity at the specified location.
     *
     * @param world The world to spawn in
     * @param team  The scoreboard team for the marker
     * @param id    The selector identifier
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     */
    private static void spawnMarker(@NotNull World world,
                                    @NotNull Team team,
                                    @NotNull String id,
                                    double x, double y, double z) {
        Location location = new Location(world, x + 0.5, y, z + 0.5);

        Slime slime = (Slime) world.spawnEntity(location, EntityType.SLIME);

        // Configure slime properties for optimal visualization
        slime.setSize(2);
        slime.setAI(false);
        slime.setGravity(false);
        slime.setCollidable(false);
        slime.setSilent(true);
        slime.setCanPickupItems(false);
        slime.setGlowing(true);
        slime.setInvulnerable(true);
        slime.setInvisible(true);
        slime.setPersistent(true);
        slime.setRemoveWhenFarAway(false);

        // Add tracking tag and team
        slime.addScoreboardTag(TAG_PREFIX + id);
        team.addEntity(slime);
    }

    /**
     * Updates the glow color of an existing selector.
     *
     * @param id       The selector identifier
     * @param newColor The new glow color
     */
    public static void updateSelectorColor(@NotNull String id, @NotNull NamedTextColor newColor) {
        String fullTag = TAG_PREFIX + id;
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains(fullTag)) {
                    // Find the entity's team and update its color
                    for (Team team : board.getTeams()) {
                        if (team.hasEntity(entity)) {
                            team.color(newColor);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Counts the number of selector entities with a specific tag.
     *
     * @param id The selector identifier
     * @return The number of selector entities found
     */
    public static int countSelectors(@NotNull String id) {
        String fullTag = TAG_PREFIX + id;
        int count = 0;

        for (World world : Bukkit.getWorlds()) {
            count += (int) world.getEntities().stream()
                    .filter(entity -> entity.getScoreboardTags().contains(fullTag))
                    .count();
        }

        return count;
    }

    /**
     * Checks if a selector with the given ID exists.
     *
     * @param id The selector identifier
     * @return true if at least one selector entity exists
     */
    public static boolean selectorExists(@NotNull String id) {
        return countSelectors(id) > 0;
    }

    /**
     * Gets all entities belonging to a specific selector.
     *
     * @param id The selector identifier
     * @return List of selector entities
     */
    @NotNull
    public static java.util.List<Entity> getSelectorEntities(@NotNull String id) {
        String fullTag = TAG_PREFIX + id;
        java.util.List<Entity> entities = new java.util.ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(entity -> entity.getScoreboardTags().contains(fullTag))
                    .forEach(entities::add);
        }

        return entities;
    }

    /**
     * Resets the team counter.
     * Useful for testing or when cleaning up all selectors.
     */
    public static void resetTeamCounter() {
        teamCounter.set(0);
    }

    /**
     * Gets the current team counter value.
     *
     * @return The current team counter
     */
    public static int getTeamCounter() {
        return teamCounter.get();
    }
}