package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.dumbdogdiner.dogcore.Permissions
import org.bukkit.entity.Player

fun invSeeCommand() = commandAPICommand("invsee") {
    playerArgument("player")
    withPermission(Permissions.SNOOP)

    playerExecutor { sender, args ->
        // TODO we could look into offline player support
        // TODO does not show armor slots/offhand
        val player = args["player"] as Player
        sender.openInventory(player.inventory)
    }
}
