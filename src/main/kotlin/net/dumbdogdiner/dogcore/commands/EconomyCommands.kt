package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.User
import net.dumbdogdiner.dogcore.messages.Messages
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Range
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

object EconomyCommands {
    @Command("balance", "bal")
    @CommandPermission(Permissions.ECO)
    fun balance(
        sender: CommandSender,
        @Optional
        player: OfflinePlayer?
    ) {
        val playerToLookup = player ?: (sender as? Player)
            ?: commandError(Messages["error.playerNeeded"])

        val user = User.lookupCommand(playerToLookup)
        val balance = user.balance
        if (playerToLookup == sender) {
            sender.sendMessage(Messages["commands.balance.query", Component.text(balance)])
        } else {
            sender.sendMessage(Messages["commands.balance.query.other", user.formattedName(), Component.text(balance)])
        }
    }

    @Command("balancetop", "baltop")
    @CommandPermission(Permissions.ECO)
    fun balanceTop(
        sender: CommandSender,
        @Default("1")
        @Range(min = 1.0)
        page: Int
    ) {
        for ((username, amount) in User.top(page)) {
            sender.sendMessage(Messages["commands.balancetop.entry", username, Component.text(amount)])
        }
    }

    @Command("pay")
    @CommandPermission(Permissions.ECO)
    fun pay(
        sender: Player,
        player: OfflinePlayer,
        @Range(min = 1.0)
        amount: Long
    ) {
        val from = User.lookupCommand(sender)
        val to = User.lookupCommand(player)
        if (from.pay(to, amount)) {
            sender.sendMessage(Messages["commands.pay.success", Component.text(amount), to.formattedName()])
        } else {
            sender.sendMessage(Messages["error.failedTransaction"])
        }
    }

    private fun economyHelper(sender: CommandSender, player: OfflinePlayer, action: (User) -> Boolean) {
        val user = User.lookupCommand(player)
        if (action(user)) {
            sender.sendMessage(Messages["commands.economy.success"])
        } else {
            sender.sendMessage(Messages["error.failedTransaction"])
        }
    }

    @Command("economy", "eco")
    @Subcommand("give")
    @CommandPermission(Permissions.ECO_ADMIN)
    fun economyGive(
        sender: CommandSender,
        player: OfflinePlayer,
        @Range(min = 1.0)
        amount: Long
    ) {
        economyHelper(sender, player) { it.give(amount) }
    }

    @Command("economy", "eco")
    @Subcommand("take")
    @CommandPermission(Permissions.ECO_ADMIN)
    fun economyTake(
        sender: CommandSender,
        player: OfflinePlayer,
        @Range(min = 1.0)
        amount: Long
    ) {
        economyHelper(sender, player) { it.give(-amount) }
    }

    @Command("economy", "eco")
    @Subcommand("set")
    @CommandPermission(Permissions.ECO_ADMIN)
    fun economySet(sender: CommandSender, player: OfflinePlayer, amount: Long) {
        economyHelper(sender, player) {
            it.balance = amount
            true
        }
    }
}
