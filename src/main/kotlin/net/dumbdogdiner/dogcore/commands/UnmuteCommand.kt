package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

fun unmuteCommand() = commandAPICommand("unmute") {
    withPermission("dogcore.unmute")

    offlinePlayerArgument("player")

    anyExecutor { sender, args ->
        val player = DbPlayer.lookup(args["player"] as OfflinePlayer)
        if (player == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return@anyExecutor
        }
        player.unmute()
    }
}
