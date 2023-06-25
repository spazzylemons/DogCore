package net.dumbdogdiner.dogcore.teleport;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public final class TeleportHelper {
    private TeleportHelper() { }

    /** The radius to search for teleport locations in. */
    private static final int RADIUS = 3;

    /** The length of one dimension of the offset cube. */
    private static final int DIAMETER = (RADIUS * 2) + 1;


    /** The offset to convert a coordinate to the center of the block. */
    private static final double BLOCK_CENTER = 0.5;

    /** The offsets to look for safe teleport positions. */
    private static final Vector3i[] OFFSETS = new Vector3i[DIAMETER * DIAMETER * DIAMETER];

    /** The key for the Overworld. */
    private static final NamespacedKey OVERWORLD = NamespacedKey.minecraft("overworld");

    static {
        var i = 0;
        for (var x = -RADIUS; x <= RADIUS; x++) {
            for (var y = -RADIUS; y <= RADIUS; y++) {
                for (var z = -RADIUS; z <= RADIUS; z++) {
                    OFFSETS[i++] = new Vector3i(x, y, z);
                }
            }
        }
        Arrays.sort(OFFSETS, Comparator.comparingLong(Vector3i::lengthSquared));
    }

    /**
     * These materials were selected by a script, to fit the following criteria.
     * - If spawned above the block, the player must be able to land on the block.
     * - The block must be tall enough such that the player does not receive effects from the block below.
     * - This must hold true for all block states.
     * Additionally, extra blocks were removed for the given reasons:
     * - Pointed dripstone deals extra fall damage
     * - Turtle eggs can break if a player lands or steps on them
     * - Cauldrons filled with lava can be fallen into
     * - Cacti, magma blocks, campfires and soul campfires hurt the player
     * - Sculk sensors and sculk shriekers may result in a Warden encounter
     *   - Teleporting nearby one of these blocks could result in the same effect, however...
     * Also, a special exception was made to allow water as a block that is "safe to stand on", so that you can
     * teleport to the ocean.
     */
    private static final Set<Material> SAFE_TO_STAND_ON = new HashSet<>();

    /**
     * These materials were selected manually as blocks you would not want to teleport into.
     * - Portals can teleport you somewhere else
     * - Pressure plates and tripwires can activate traps
     * - Fire, soul fire, and lava will burn
     * - Sweet berry bushes and wither roses will hurt
     */
    private static final Set<Material> HARMFUL_PASSABLE = new HashSet<>();

    /**
     * A map of materials, used for initialization.
     */
    private static final Map<String, Material> MATERIAL_MAP = new HashMap<>();

    private static void parseMaterialList(
        final @NotNull JavaPlugin plugin,
        final @NotNull String filename,
        final @NotNull Set<Material> set
    ) {
        try (var resource = plugin.getResource(filename)) {
            if (resource == null) {
                throw new RuntimeException("Missing safe teleport resource " + filename);
            }
            var scanner = new Scanner(resource);
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine();
                if (line.charAt(0) == '_') {
                    for (var entry : MATERIAL_MAP.entrySet()) {
                        if (entry.getKey().endsWith(line)) {
                            set.add(entry.getValue());
                        }
                    }
                } else if (line.charAt(line.length() - 1) == '_') {
                    for (var entry : MATERIAL_MAP.entrySet()) {
                        if (entry.getKey().startsWith(line)) {
                            set.add(entry.getValue());
                        }
                    }
                } else {
                    set.add(MATERIAL_MAP.get(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the safe teleport system.
     * @param plugin The plugin.
     */
    public static void initSafeTeleport(final @NotNull JavaPlugin plugin) {
        for (var mat : Material.values()) {
            if (!mat.isLegacy()) {
                // only consider if not legacy
                MATERIAL_MAP.put(mat.name(), mat);
            }
        }
        parseMaterialList(plugin, "safe_blocks.txt", SAFE_TO_STAND_ON);
        parseMaterialList(plugin, "harmful_passable.txt", HARMFUL_PASSABLE);
        MATERIAL_MAP.clear();
    }

    private static boolean isSafeTeleport(
        final @NotNull World world,
        final int x,
        final int y,
        final int z
    ) {
        var block = world.getBlockAt(x, y, z);
        // check that the block we're going to has space for us and won't hurt us
        if (block.isCollidable() || HARMFUL_PASSABLE.contains(block.getType())) {
            return false;
        }
        // check that the block above won't hurt us
        // it's okay if it's solid because we'll crawl
        var above = world.getBlockAt(x, y + 1, z);
        if (HARMFUL_PASSABLE.contains(above.getType())) {
            return false;
        }
        // check that the block below won't hurt us
        var below = world.getBlockAt(x, y - 1, z);
        return SAFE_TO_STAND_ON.contains(below.getType());
    }

    public static @NotNull Location getSafeTeleport(final @NotNull Location destination) {
        var world = destination.getWorld();
        Preconditions.checkNotNull(world);
        var blockX = destination.getBlockX();
        var blockY = (int) Math.round(destination.getY());
        var blockZ = destination.getBlockZ();
        // move down until we hit a non-air block
        var origY = blockY;
        for (;;) {
            if (blockY - 1 < world.getMinHeight()) {
                blockY = origY;
                break;
            }
            if (!world.getBlockAt(blockX, blockY - 1, blockZ).isEmpty()) {
                break;
            }
            --blockY;
        }
        // find a safe block to go to
        for (var offset : OFFSETS) {
            var x = offset.x + blockX;
            var y = offset.y + blockY;
            var z = offset.z + blockZ;
            if (isSafeTeleport(world, x, y, z)) {
                var newX = x + BLOCK_CENTER;
                var newZ = z + BLOCK_CENTER;
                var yaw = destination.getYaw();
                var pitch = destination.getPitch();
                return new Location(world, newX, y, newZ, yaw, pitch);
            }
        }
        // nearby spots are not safe, try moving up
        while (!isSafeTeleport(world, blockX, blockY, blockZ)) {
            ++blockY;
            if (blockY > world.getMaxHeight()) {
                // give up
                return destination;
            }
        }
        var newX = blockX + BLOCK_CENTER;
        var newZ = blockZ + BLOCK_CENTER;
        var yaw = destination.getYaw();
        var pitch = destination.getPitch();
        return new Location(world, newX, blockY, newZ, yaw, pitch);
    }

    public static void safeTeleport(final @NotNull Player player, final @NotNull Location destination) {
        player.teleportAsync(getSafeTeleport(destination));
    }

    public static @NotNull Location getSpawnLocation() {
        var world = Bukkit.getServer().getWorld(OVERWORLD);
        if (world == null) {
            throw new IllegalStateException("Could not get overworld");
        }
        return world.getSpawnLocation().clone().add(BLOCK_CENTER, 0.0, BLOCK_CENTER);
    }
}
