package net.dumbdogdiner.dogcore.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ChatFormatter : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = DbPlayer(event.player.uniqueId)
        if (player.isMuted) {
            // if player is muted, tell them that, and don't send the message
            event.isCancelled = true
            event.player.sendMessage(Component.text("Message not sent because you are muted."))
        } else {
            // format message
            event.renderer { _, sourceDisplayName, message, _ ->
                Component.textOfChildren(
                    Component.text("uwu, "),
                    sourceDisplayName,
                    Component.text(" says "),
                    message
                )
            }
        }
    }
}
