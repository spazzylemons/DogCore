package net.dumbdogdiner.dogcore.listener

import net.dumbdogdiner.dogcore.chat.PrefixManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object TabListFormatter : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        PrefixManager.updatePlayerListName(event.player)
    }
}
