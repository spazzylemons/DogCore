package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.longArgument
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

fun ecoCommand() = commandAPICommand("eco") {
    withAliases("economy")
    withPermission("dogcore.eco")

    multiLiteralArgument("give", "take", "set")
    offlinePlayerArgument("player")
    longArgument("amount", min = 0)

    anyExecutor { sender, args ->
        val player = DbPlayer.lookup(args["player"] as OfflinePlayer)
            ?: run {
                sender.sendMessage(Component.text("Player argument needed in this context."))
                return@anyExecutor
            }
        val amount = args["amount"] as Long
        val success = when (args[0]) {
            "give" -> player.give(amount)
            "take" -> player.give(-amount)
            "set" -> {
                player.balance = amount
                true
            }
            else -> return@anyExecutor
        }
        if (success) {
            sender.sendMessage(Component.text("The balance was updated"))
        } else {
            sender.sendMessage(Component.text("The balance would overflow, so it was not updated."))
        }
    }
}
