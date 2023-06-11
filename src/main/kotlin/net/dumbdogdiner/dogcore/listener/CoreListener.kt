package net.dumbdogdiner.dogcore.listener

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.dumbdogdiner.dogcore.DogCorePlugin
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class CoreListener(private val plugin: DogCorePlugin) : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = DbPlayer.lookup(event.player) ?: return
        if (player.isMuted) {
            // if player is muted, tell them that, and don't send the message
            event.isCancelled = true
            event.player.sendMessage(Component.text("Message not sent because you are muted."))
        } else {
            // format message
            event.renderer { _, _, message, _ ->
                Component.textOfChildren(
                    runBlocking { NameFormatter.formatUsername(event.player) },
                    Component.text(" » ", NamedTextColor.DARK_GRAY),
                    message
                )
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        // register player, if not registered already
        val firstJoin = DbPlayer.register(player)
        // set their tab list name
        runBlocking {
            NameFormatter.updatePlayerListName(player)
        }
        // remove access to some vanilla commands
        plugin.removeVanillaOverrides(player)
        // announce join if new to server
        if (firstJoin) {
            plugin.server.broadcast(Component.text("Welcome to the server, ${player.name}!"))
        }
    }
}
