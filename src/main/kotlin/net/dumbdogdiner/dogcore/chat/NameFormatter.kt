package net.dumbdogdiner.dogcore.chat

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.group.GroupDataRecalculateEvent
import net.luckperms.api.model.user.User
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

/**
 * Manages chat prefixes and colors by permission group.
 */
object NameFormatter {
    private val lp = LuckPermsProvider.get()

    private lateinit var server: Server

    private fun parseColor(color: String) = NamedTextColor.NAMES.value(color)
        ?: TextColor.fromCSSHexString(color)

    private suspend fun getOrLoadUser(uuid: UUID, name: String): User {
        return lp.userManager.getUser(uuid) ?: suspendCancellableCoroutine { continuation ->
            // spawn a thread to block for the continuation
            Thread {
                // operation executes in another thread, catching exceptions to propagate to coroutine
                continuation.resumeWith(runCatching { lp.userManager.loadUser(uuid, name).get() })
            }.start()
        }
    }

    private suspend fun formatUsername(uuid: UUID, name: String): Component {
        val metadata = lp.groupManager.getGroup(getOrLoadUser(uuid, name).primaryGroup)?.cachedData?.metaData
        val rank = metadata?.getMetaValue("rank")
        val color = metadata?.getMetaValue("color")?.let { parseColor(it) } ?: NamedTextColor.GRAY
        return if (rank != null) {
            Component.textOfChildren(
                Component.text("[$rank]", color),
                Component.space(),
                Component.text(name, color)
            )
        } else {
            Component.text(name, color)
        }
    }

    suspend fun formatUsername(player: Player) = formatUsername(player.uniqueId, player.name)

    fun init(plugin: JavaPlugin) {
        server = plugin.server
        val eventBus = lp.eventBus
        eventBus.subscribe(plugin, GroupDataRecalculateEvent::class.java) { invalidate() }
    }

    suspend fun updatePlayerListName(player: Player) {
        player.playerListName(formatUsername(player))
    }

    private fun invalidate() {
        runBlocking {
            server.onlinePlayers.map { player ->
                async {
                    updatePlayerListName(player)
                }
            }.awaitAll()
        }
    }
}
