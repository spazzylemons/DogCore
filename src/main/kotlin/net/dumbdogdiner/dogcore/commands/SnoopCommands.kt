package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

object SnoopCommands {
    @Command("invsee")
    @CommandPermission(Permissions.SNOOP)
    fun invSee(sender: Player, player: Player) {
        // TODO we could look into offline player support
        // TODO does not show armor slots/offhand
        sender.openInventory(player.inventory)
    }
}
