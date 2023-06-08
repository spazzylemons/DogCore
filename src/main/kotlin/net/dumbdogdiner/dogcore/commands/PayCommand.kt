package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.longArgument
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

fun payCommand() = commandAPICommand("pay") {
    withPermission("dogcore.pay")

    offlinePlayerArgument("player")
    longArgument("amount", min = 0)

    playerExecutor { sender, args ->
        val s = DbPlayer.lookup(sender) ?: return@playerExecutor
        val player = DbPlayer.lookup(args["player"] as OfflinePlayer)
            ?: run {
                sender.sendMessage(Component.text("Cannot pay a player who has never joined."))
                return@playerExecutor
            }
        val amount = args["amount"] as Long
        if (s.pay(player, amount)) {
            sender.sendMessage(Component.text("You paid $amount bean(s) to ${player.username}."))
        } else {
            sender.sendMessage(Component.text("Could not complete the transaction."))
        }
    }
}
