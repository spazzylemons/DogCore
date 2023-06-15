package net.dumbdogdiner.dogcore.teleport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public final class SafeTeleport {
    private SafeTeleport() { }

    /** The radius to search for teleport locations in. */
    private static final int RADIUS = 3;

    /** The offset to convert a coordinate to the center of the block. */
    private static final double BLOCK_CENTER = 0.5;

    /** The offsets to look for safe teleport positions. */
    private static final List<Vector3i> OFFSETS = new ArrayList<>();

    static {
        for (var x = -RADIUS; x <= RADIUS; x++) {
            for (var y = -RADIUS; y <= RADIUS; y++) {
                for (var z = -RADIUS; z <= RADIUS; z++) {
                    OFFSETS.add(new Vector3i(x, y, z));
                }
            }
        }
        OFFSETS.sort(Comparator.comparingLong(Vector3i::lengthSquared));
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
        @NotNull final JavaPlugin plugin,
        @NotNull final String filename,
        @NotNull final Set<Material> set
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
    public static void initSafeTeleport(@NotNull final JavaPlugin plugin) {
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
        @NotNull final World world,
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
        // also try to avoid drowning the player
        var above = world.getBlockAt(x, y + 1, z);
        if (HARMFUL_PASSABLE.contains(above.getType()) || above.getType() == Material.WATER) {
            return false;
        }
        // check that the block below won't hurt us
        var below = world.getBlockAt(x, y - 1, z);
        return SAFE_TO_STAND_ON.contains(below.getType());
    }

    public static void safeTeleport(
        @NotNull final Player player,
        @NotNull final Location destination
    ) {
        var world = destination.getWorld();
        var blockX = destination.getBlockX();
        var blockY = destination.getBlockY();
        var blockZ = destination.getBlockZ();
        for (var offset : OFFSETS) {
            var x = offset.x + blockX;
            var y = offset.y + blockY;
            var z = offset.z + blockZ;
            if (isSafeTeleport(world, x, y, z)) {
                var newX = x + BLOCK_CENTER;
                var newZ = z + BLOCK_CENTER;
                var yaw = destination.getYaw();
                var pitch = destination.getPitch();
                player.teleportAsync(new Location(world, newX, y, newZ, yaw, pitch));
                return;
            }
        }
    }
}
