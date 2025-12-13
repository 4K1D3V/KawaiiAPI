package dev.oumaimaa.kawaiiapi.region;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a cuboid region defined by two corner points.
 * Provides modern utilities for region manipulation, entity queries, and block operations
 * without deprecated APIs.
 *
 * <p>Features:
 * <ul>
 *   <li>Efficient entity retrieval using modern chunk-based queries</li>
 *   <li>Region overlap detection</li>
 *   <li>Region expansion and contraction</li>
 *   <li>Type-safe entity filtering</li>
 *   <li>Comprehensive boundary calculations</li>
 * </ul>
 *
 * @author KawaiiDevelopment
 * @version 1.0
 */
@Data
public class Region {

    private Location corner1;
    private Location corner2;

    /**
     * Creates an uninitialized region with null corners.
     */
    public Region() {
        this.corner1 = null;
        this.corner2 = null;
    }

    /**
     * Creates a region with the specified corner locations.
     *
     * @param corner1 The first corner location
     * @param corner2 The second corner location
     * @throws IllegalArgumentException if corners are in different worlds or null
     */
    public Region(@NotNull Location corner1, @NotNull Location corner2) {
        if (!corner1.getWorld().equals(corner2.getWorld())) {
            throw new IllegalArgumentException("Both corners must be in the same world");
        }
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    /**
     * Checks if both corners of the region are set.
     *
     * @return true if the region is fully defined
     */
    public boolean isSet() {
        return corner1 != null && corner2 != null;
    }

    /**
     * Gets the world this region is located in.
     *
     * @return The world, or null if region is not set
     */
    @Nullable
    public World getWorld() {
        return corner1 != null ? corner1.getWorld() : null;
    }

    /**
     * Checks if a location is within this region.
     *
     * @param loc The location to check
     * @return true if the location is inside the region
     * @throws IllegalStateException if the region is not fully set
     */
    public boolean isIn(@NotNull Location loc) {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        if (!loc.getWorld().equals(getWorld())) {
            return false;
        }

        double xMin = Math.min(corner1.getX(), corner2.getX());
        double xMax = Math.max(corner1.getX(), corner2.getX());
        double yMin = Math.min(corner1.getY(), corner2.getY());
        double yMax = Math.max(corner1.getY(), corner2.getY());
        double zMin = Math.min(corner1.getZ(), corner2.getZ());
        double zMax = Math.max(corner1.getZ(), corner2.getZ());

        return loc.getX() >= xMin && loc.getX() <= xMax &&
                loc.getY() >= yMin && loc.getY() <= yMax &&
                loc.getZ() >= zMin && loc.getZ() <= zMax;
    }

    /**
     * Gets the total number of blocks in this region.
     *
     * @return The block count
     */
    public int getTotalBlockSize() {
        return (int) (getHeight() * getXWidth() * getZWidth());
    }

    /**
     * Gets the height (Y-axis) of the region.
     *
     * @return The height in blocks
     */
    public double getHeight() {
        if (!isSet()) return 0;
        double yMin = Math.min(corner1.getY(), corner2.getY());
        double yMax = Math.max(corner1.getY(), corner2.getY());
        return yMax - yMin + 1;
    }

    /**
     * Gets the width (X-axis) of the region.
     *
     * @return The width in blocks
     */
    public double getXWidth() {
        if (!isSet()) return 0;
        double xMin = Math.min(corner1.getX(), corner2.getX());
        double xMax = Math.max(corner1.getX(), corner2.getX());
        return xMax - xMin + 1;
    }

    /**
     * Gets the depth (Z-axis) of the region.
     *
     * @return The depth in blocks
     */
    public double getZWidth() {
        if (!isSet()) return 0;
        double zMin = Math.min(corner1.getZ(), corner2.getZ());
        double zMax = Math.max(corner1.getZ(), corner2.getZ());
        return zMax - zMin + 1;
    }

    /**
     * Gets a list of all blocks within this region.
     * WARNING: This can be very memory-intensive for large regions.
     * Consider using streaming methods for large regions.
     *
     * @return List of all blocks in the region
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public List<Block> blockList() {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        World world = getWorld();
        if (world == null) {
            return new ArrayList<>();
        }

        double xMin = Math.min(corner1.getX(), corner2.getX());
        double xMax = Math.max(corner1.getX(), corner2.getX());
        double yMin = Math.min(corner1.getY(), corner2.getY());
        double yMax = Math.max(corner1.getY(), corner2.getY());
        double zMin = Math.min(corner1.getZ(), corner2.getZ());
        double zMax = Math.max(corner1.getZ(), corner2.getZ());

        List<Block> blocks = new ArrayList<>(getTotalBlockSize());

        for (double x = xMin; x <= xMax; x++) {
            for (double y = yMin; y <= yMax; y++) {
                for (double z = zMin; z <= zMax; z++) {
                    Block block = world.getBlockAt((int) x, (int) y, (int) z);
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    /**
     * Checks if a player is within this region.
     *
     * @param player The player to check
     * @return true if the player is in the region
     */
    public boolean isPlayerIn(@NotNull Player player) {
        return isIn(player.getLocation());
    }

    /**
     * Gets all entities within this region using modern chunk-based search.
     * More efficient than the old block-by-block approach.
     *
     * @return List of entities in the region
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public List<Entity> getEntities() {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        World world = getWorld();
        if (world == null) {
            return new ArrayList<>();
        }

        Location center = getCenter();
        double xRadius = getXWidth() / 2.0 + 1;
        double yRadius = getHeight() / 2.0 + 1;
        double zRadius = getZWidth() / 2.0 + 1;

        // Use modern Paper API for efficient entity retrieval
        Collection<Entity> nearbyEntities = world.getNearbyEntities(
                center,
                xRadius,
                yRadius,
                zRadius
        );

        // Filter entities to only those within exact bounds
        double xMin = Math.min(corner1.getX(), corner2.getX());
        double xMax = Math.max(corner1.getX(), corner2.getX());
        double yMin = Math.min(corner1.getY(), corner2.getY());
        double yMax = Math.max(corner1.getY(), corner2.getY());
        double zMin = Math.min(corner1.getZ(), corner2.getZ());
        double zMax = Math.max(corner1.getZ(), corner2.getZ());

        return nearbyEntities.stream()
                .filter(entity -> {
                    Location loc = entity.getLocation();
                    return loc.getX() >= xMin && loc.getX() <= xMax &&
                            loc.getY() >= yMin && loc.getY() <= yMax &&
                            loc.getZ() >= zMin && loc.getZ() <= zMax;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets all entities of a specific type within this region.
     *
     * @param entityClass The class of entities to filter for
     * @param <T>         The entity type
     * @return List of entities matching the type
     */
    @NotNull
    public <T extends Entity> List<T> getEntitiesByType(@NotNull Class<T> entityClass) {
        return getEntities().stream()
                .filter(entityClass::isInstance)
                .map(entityClass::cast)
                .collect(Collectors.toList());
    }

    /**
     * Gets all players within this region.
     *
     * @return List of players in the region
     */
    @NotNull
    public List<Player> getPlayers() {
        return getEntitiesByType(Player.class);
    }

    /**
     * Gets the center location of the region.
     *
     * @return The center location
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public Location getCenter() {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        double x = (corner1.getX() + corner2.getX()) / 2.0;
        double y = (corner1.getY() + corner2.getY()) / 2.0;
        double z = (corner1.getZ() + corner2.getZ()) / 2.0;

        return new Location(getWorld(), x, y, z);
    }

    /**
     * Gets the minimum corner point of the region.
     *
     * @return Location of the minimum corner (lowest x, y, z)
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public Location getMinimumPoint() {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        double x = Math.min(corner1.getX(), corner2.getX());
        double y = Math.min(corner1.getY(), corner2.getY());
        double z = Math.min(corner1.getZ(), corner2.getZ());

        return new Location(getWorld(), x, y, z);
    }

    /**
     * Gets the maximum corner point of the region.
     *
     * @return Location of the maximum corner (highest x, y, z)
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public Location getMaximumPoint() {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        double x = Math.max(corner1.getX(), corner2.getX());
        double y = Math.max(corner1.getY(), corner2.getY());
        double z = Math.max(corner1.getZ(), corner2.getZ());

        return new Location(getWorld(), x, y, z);
    }

    /**
     * Expands the region in all directions by the specified amount.
     *
     * @param amount The amount to expand by (in blocks)
     * @return This region instance for method chaining
     * @throws IllegalStateException if the region is not fully set
     */
    @NotNull
    public Region expand(int amount) {
        if (!isSet()) {
            throw new IllegalStateException("Region corners are not set");
        }

        Location min = getMinimumPoint();
        Location max = getMaximumPoint();

        corner1 = new Location(getWorld(),
                min.getX() - amount,
                min.getY() - amount,
                min.getZ() - amount);
        corner2 = new Location(getWorld(),
                max.getX() + amount,
                max.getY() + amount,
                max.getZ() + amount);

        return this;
    }

    /**
     * Contracts the region in all directions by the specified amount.
     *
     * @param amount The amount to contract by (in blocks)
     * @return This region instance for method chaining
     */
    @NotNull
    public Region contract(int amount) {
        return expand(-amount);
    }

    /**
     * Checks if this region overlaps with another region.
     *
     * @param other The other region to check
     * @return true if the regions overlap
     */
    public boolean overlaps(@NotNull Region other) {
        if (!isSet() || !other.isSet()) {
            return false;
        }

        if (!getWorld().equals(other.getWorld())) {
            return false;
        }

        Location thisMin = getMinimumPoint();
        Location thisMax = getMaximumPoint();
        Location otherMin = other.getMinimumPoint();
        Location otherMax = other.getMaximumPoint();

        return thisMax.getX() >= otherMin.getX() && thisMin.getX() <= otherMax.getX() &&
                thisMax.getY() >= otherMin.getY() && thisMin.getY() <= otherMax.getY() &&
                thisMax.getZ() >= otherMin.getZ() && thisMin.getZ() <= otherMax.getZ();
    }

    /**
     * Checks if this region completely contains another region.
     *
     * @param other The region to check
     * @return true if this region contains the other region
     */
    public boolean contains(@NotNull Region other) {
        if (!isSet() || !other.isSet()) {
            return false;
        }

        if (!getWorld().equals(other.getWorld())) {
            return false;
        }

        return isIn(other.getMinimumPoint()) && isIn(other.getMaximumPoint());
    }

    /**
     * Creates a copy of this region.
     *
     * @return A new Region instance with the same corners
     */
    @NotNull
    public Region clone() {
        Region cloned = new Region();
        if (isSet()) {
            cloned.setCorner1(corner1.clone());
            cloned.setCorner2(corner2.clone());
        }
        return cloned;
    }

    @Override
    public String toString() {
        if (!isSet()) {
            return "Region{unset}";
        }
        return String.format("Region{world=%s, min=%s, max=%s, volume=%d}",
                getWorld().getName(),
                formatLocation(getMinimumPoint()),
                formatLocation(getMaximumPoint()),
                getTotalBlockSize());
    }

    /**
     * Formats a location as a compact string.
     *
     * @param loc The location to format
     * @return Formatted string
     */
    @NotNull
    private String formatLocation(@NotNull Location loc) {
        return String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }
}