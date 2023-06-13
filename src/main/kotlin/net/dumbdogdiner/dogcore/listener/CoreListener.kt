package net.dumbdogdiner.dogcore.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.dumbdogdiner.dogcore.DogCorePlugin
import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.commands.BackCommand
import net.dumbdogdiner.dogcore.db.User
import net.dumbdogdiner.dogcore.messages.Messages
import net.dumbdogdiner.dogcore.teleport.TpaManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class CoreListener(private val plugin: DogCorePlugin) : Listener {
    private val death = DeathMessageRandomizer(plugin)

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = User.lookup(event.player) ?: return
        if (player.isMuted) {
            // if player is muted, tell them that, and don't send the message
            event.isCancelled = true
            event.player.sendMessage(Messages["error.muted"])
        } else {
            // format message
            event.renderer { _, _, message, _ ->
                Messages["chat", NameFormatter.formatUsername(event.player).get(), message]
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        // register player, if not registered already
        val firstJoin = User.register(player)
        // set their tab list name
        val name = NameFormatter.formatUsername(player).get()
        player.displayName(name)
        player.playerListName(name)
        // remove access to some vanilla commands
        plugin.removeVanillaOverrides(player)
        // announce join
        event.joinMessage(Messages["chat.join", name])
        // welcome message if new to server
        if (firstJoin) {
            plugin.server.broadcast(Messages["chat.welcome", Component.text(player.name)])
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val name = NameFormatter.formatUsername(event.player).get()
        event.quitMessage(Messages["chat.quit", name])
        TpaManager.removePlayer(event.player.uniqueId)
        BackCommand.removeBack(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.entity.lastDamageCause?.let { lastDamage ->
            var attacker = when (lastDamage) {
                is EntityDamageByBlockEvent -> lastDamage.damager?.type?.translationKey()
                    ?.let { Component.translatable(it) }
                is EntityDamageByEntityEvent -> lastDamage.damager.let {
                    if (it is Player) {
                        NameFormatter.formatUsername(it).get()
                    } else {
                        it.name()
                    }
                }
                else -> null
            }
            val message = death.select(lastDamage.cause, NameFormatter.formatUsername(event.player).get(), attacker)
            if (message != null) {
                event.deathMessage(message)
            }
        }
        if (event.player.hasPermission(Permissions.BACK)) {
            event.player.sendMessage(Messages["chat.back"].clickEvent(ClickEvent.runCommand("/back")))
            BackCommand.setBack(event.player.uniqueId, event.player.location)
        }
    }
}
