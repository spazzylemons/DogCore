package net.dumbdogdiner.dogcore.listener

import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PlayerRegistrar : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (DbPlayer.register(event.player)) {
            event.player.server.broadcast(Component.text("Welcome to the server, ${event.player.name}!"))
        }
    }
}
