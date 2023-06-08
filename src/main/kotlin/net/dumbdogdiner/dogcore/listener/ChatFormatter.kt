package net.dumbdogdiner.dogcore.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.dumbdogdiner.dogcore.chat.PrefixManager
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ChatFormatter : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = DbPlayer.lookup(event.player) ?: return
        if (player.isMuted) {
            // if player is muted, tell them that, and don't send the message
            event.isCancelled = true
            event.player.sendMessage(Component.text("Message not sent because you are muted."))
        } else {
            // format message
            val prefix = PrefixManager[event.player.uniqueId]
            val color = prefix.color ?: NamedTextColor.GRAY
            val username = event.player.name
            val name = prefix.name?.let { name ->
                Component.textOfChildren(
                    Component.text(name, color, TextDecoration.BOLD),
                    Component.text(" » ", NamedTextColor.DARK_GRAY),
                    Component.text(username, color)
                )
            } ?: Component.text(username, color)

            event.renderer { _, _, message, _ ->
                Component.textOfChildren(
                    name,
                    Component.text(": ", NamedTextColor.DARK_GRAY),
                    message
                )
            }
        }
    }
}
