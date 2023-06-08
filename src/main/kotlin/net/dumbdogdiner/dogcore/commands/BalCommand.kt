package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

fun balCommand() = commandAPICommand("bal") {
    withAliases("balance")
    offlinePlayerArgument("player", optional = true)
    withPermission("dogcore.bal")

    anyExecutor { sender, args ->
        val p = args["player"] as OfflinePlayer?
            ?: sender as? Player
            ?: run {
                sender.sendMessage(Component.text("Player argument needed in this context."))
                return@anyExecutor
            }
        val player = DbPlayer.lookup(p)
        if (player == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return@anyExecutor
        }
        val balance = player.balance
        if (p == sender) {
            sender.sendMessage(Component.text("You have $balance bean(s)."))
        } else {
            sender.sendMessage(Component.text("${player.username} has $balance bean(s)."))
        }
    }
}
