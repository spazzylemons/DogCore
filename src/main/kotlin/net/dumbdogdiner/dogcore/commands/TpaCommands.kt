package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.teleport.TpaManager
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command

object TpaCommands {
    @Command("tpask", "tpa")
    fun tpa(sender: Player, player: Player) {
        TpaManager.request(sender, player, false)
    }

    @Command("tpahere")
    fun tpaHere(sender: Player, player: Player) {
        TpaManager.request(sender, player, true)
    }

    @Command("tpaccept")
    fun tpAccept(sender: Player, player: Player) {
        TpaManager.accept(sender, player)
    }

    @Command("tpdeny")
    fun tpDeny(sender: Player, player: Player) {
        TpaManager.deny(sender, player)
    }
}
