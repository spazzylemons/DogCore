package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.teleport.TpaManager
import net.dumbdogdiner.dogcore.util.CoroutineThreadPool
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

object TpaCommands {
    @Command("tpask", "tpa")
    @CommandPermission(Permissions.TPA)
    fun tpa(sender: Player, player: Player) {
        CoroutineThreadPool.launch {
            TpaManager.request(sender, player, false)
        }
    }

    @Command("tpahere")
    fun tpaHere(sender: Player, player: Player) {
        CoroutineThreadPool.launch {
            TpaManager.request(sender, player, true)
        }
    }

    @Command("tpaccept")
    fun tpAccept(sender: Player, player: Player) {
        CoroutineThreadPool.launch {
            TpaManager.accept(sender, player)
        }
    }

    @Command("tpdeny")
    fun tpDeny(sender: Player, player: Player) {
        CoroutineThreadPool.launch {
            TpaManager.deny(sender, player)
        }
    }
}
