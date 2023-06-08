package net.dumbdogdiner.dogcore.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.event.node.NodeClearEvent
import net.luckperms.api.event.node.NodeRemoveEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

/**
 * Manages chat prefixes and colors by permission group.
 * Uses a different system than Essentials, to make things cleaner:
 * Each group has two permission nodes which control formatting:
 * dogcore.prefix.name.<name>: Sets the prefix name to <name>..
 * dogcore.prefix.color.<color>: Sets the prefix color to <color>.
 */
object PrefixManager {
    private const val NAME = "dogcore.prefix.name."
    private const val COLOR = "dogcore.prefix.color."
    private val DEFAULT = Prefix(null, null)

    private val cache = mutableMapOf<String, Prefix>()

    private val lp = LuckPermsProvider.get()

    private lateinit var server: Server

    operator fun get(uuid: UUID): Prefix {
        return lp.userManager.getUser(uuid)?.primaryGroup?.let { groupName ->
            cache[groupName] ?: run {
                var name: String? = null
                var color: TextColor? = null
                lp.groupManager.getGroup(groupName)?.let { group ->
                    for (node in group.data().toCollection()) {
                        node.contexts
                        val key = node.key
                        if (key.startsWith(NAME)) {
                            name = key.substring(NAME.length)
                        } else if (key.startsWith(COLOR)) {
                            val c = key.substring(COLOR.length)
                            color = NamedTextColor.NAMES.value(c)
                                ?: TextColor.fromHexString(c)
                                ?: color
                        }
                    }
                }
                val result = Prefix(name, color)
                cache[groupName] = result
                result
            }
        } ?: DEFAULT
    }

    fun registerEvents(plugin: JavaPlugin) {
        server = plugin.server
        val eventBus = lp.eventBus
        eventBus.subscribe(plugin, NodeAddEvent::class.java) { invalidate() }
        eventBus.subscribe(plugin, NodeRemoveEvent::class.java) { invalidate() }
        eventBus.subscribe(plugin, NodeClearEvent::class.java) { invalidate() }
    }

    fun updatePlayerListName(player: Player) {
        val prefix = this[player.uniqueId]
        val color = prefix.color
        val username = player.name
        val name = prefix.name?.let { name ->
            Component.textOfChildren(
                Component.text(name, color, TextDecoration.BOLD),
                Component.space(),
                Component.text(username, color)
            )
        } ?: Component.text(username, color)
        player.playerListName(name)
    }

    private fun invalidate() {
        cache.clear()

        for (player in server.onlinePlayers) {
            updatePlayerListName(player)
        }
    }
}
