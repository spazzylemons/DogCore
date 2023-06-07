package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import org.bukkit.entity.Player

fun unmuteCommand() = commandAPICommand("unmute") {
    playerArgument("player")

    anyExecutor { _, args ->
        val player = DbPlayer((args["player"] as Player).uniqueId)
        player.unmute()
    }
}
