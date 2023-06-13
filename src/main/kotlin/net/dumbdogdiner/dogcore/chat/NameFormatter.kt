package net.dumbdogdiner.dogcore.chat

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dumbdogdiner.dogcore.util.CoroutineThreadPool
import net.dumbdogdiner.dogcore.util.await
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
        return lp.userManager.getUser(uuid) ?: lp.userManager.loadUser(uuid, name).await()
    }

    suspend fun formatUsername(uuid: UUID, name: String): Component {
        val metadata = lp.groupManager.getGroup(getOrLoadUser(uuid, name).primaryGroup)?.cachedData?.metaData
        val rank = metadata?.getMetaValue("rank")
        val color = metadata?.getMetaValue("color")?.let { parseColor(it) } ?: NamedTextColor.WHITE
        return if (rank != null) {
            Component.textOfChildren(
                Component.text("[$rank]", color),
                Component.space(),
                Component.text(name, color)
            )
        } else {
            Component.text(name, color)
        }
            .insertion(name)
    }

    suspend fun formatUsername(player: Player): Component {
        net.dumbdogdiner.dogcore.db.User.lookup(player)?.let {
            return it.formattedName()
        } ?: run {
            return formatUsername(player.uniqueId, player.name)
        }
    }

    fun init(plugin: JavaPlugin) {
        server = plugin.server
        val eventBus = lp.eventBus
        eventBus.subscribe(plugin, GroupDataRecalculateEvent::class.java) { invalidate() }
    }

    suspend fun refreshPlayerName(player: Player) {
        val name = formatUsername(player)
        player.displayName(name)
        player.playerListName(name)
    }

    private fun invalidate() {
        CoroutineThreadPool.launch {
            server.onlinePlayers.map { player ->
                async {
                    refreshPlayerName(player)
                }
            }.awaitAll()
        }
    }
}
