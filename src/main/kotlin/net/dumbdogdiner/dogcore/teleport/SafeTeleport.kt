package net.dumbdogdiner.dogcore.teleport

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.joml.Vector3i

private const val RADIUS = 3

private val OFFSETS = run {
    val points = mutableListOf<Vector3i>()
    for (x in -RADIUS..RADIUS) {
        for (y in -RADIUS..RADIUS) {
            for (z in -RADIUS..RADIUS) {
                points.add(Vector3i(x, y, z))
            }
        }
    }
    points.sortedBy { it.lengthSquared() }
}

/**
 * These materials were selected by a script, to fit the following criteria:
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
private val SAFE_TO_STAND_ON = mutableSetOf<Material>()

/**
 * These materials were selected manually as blocks you would not want to teleport into:
 * - Portals can teleport you somewhere else
 * - Pressure plates and tripwires can activate traps
 * - Fire, soul fire, and lava will burn
 * - Sweet berry bushes and wither roses will hurt
 */
private val HARMFUL_PASSABLE = mutableSetOf<Material>()

/**
 * A map of materials, used for initialization.
 */
private val MATERIAL_MAP = mutableMapOf<String, Material>()

private fun parseMaterialList(plugin: JavaPlugin, filename: String, set: MutableSet<Material>) {
    plugin.getResource(filename)!!.reader().useLines { lines ->
        for (line in lines) {
            if (line.first() == '_') {
                for ((k, v) in MATERIAL_MAP) {
                    if (k.endsWith(line)) set.add(v)
                }
            } else if (line.last() == '_') {
                for ((k, v) in MATERIAL_MAP) {
                    if (k.startsWith(line)) set.add(v)
                }
            } else {
                set.add(MATERIAL_MAP[line]!!)
            }
        }
    }
}

fun initSafeTeleport(plugin: JavaPlugin) {
    for (mat in Material.values()) {
        if (!mat.isLegacy) {
            // only consider if not legacy
            MATERIAL_MAP[mat.name] = mat
        }
    }
    parseMaterialList(plugin, "safe_blocks.txt", SAFE_TO_STAND_ON)
    parseMaterialList(plugin, "harmful_passable.txt", HARMFUL_PASSABLE)
    MATERIAL_MAP.clear()
}

private fun isSafeTeleport(world: World, x: Int, y: Int, z: Int): Boolean {
    val block = world.getBlockAt(x, y, z)
    // check that the block we're going to has space for us and won't hurt us
    if (block.isCollidable || block.type in HARMFUL_PASSABLE) {
        return false
    }
    // check that the block above won't hurt us
    // it's okay if it's solid because we'll crawl
    val above = world.getBlockAt(x, y + 1, z)
    if (above.type in HARMFUL_PASSABLE) {
        return false
    }
    // check that the block below won't hurt us
    val below = world.getBlockAt(x, y - 1, z)
    return below.type in SAFE_TO_STAND_ON
}

fun safeTeleport(player: Player, destination: Location) {
    val world = destination.world
    val blockX = destination.blockX
    val blockY = destination.blockY
    val blockZ = destination.blockZ
    for (offset in OFFSETS) {
        val x = offset.x + blockX
        val y = offset.y + blockY
        val z = offset.z + blockZ
        if (isSafeTeleport(world, x, y, z)) {
            player.teleportAsync(Location(world, x + 0.5, y.toDouble(), z + 0.5))
            return
        }
    }
}
