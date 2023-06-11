package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
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
        val p = player
            ?: sender as? Player
            ?: run {
                sender.sendMessage(Component.text("Player argument needed in this context."))
                return
            }
        val dbPlayer = DbPlayer.lookup(p)
        if (dbPlayer == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return
        }
        val balance = dbPlayer.balance
        if (p == sender) {
            sender.sendMessage(Component.text("You have $balance bean(s)."))
        } else {
            sender.sendMessage(Component.text("${dbPlayer.username} has $balance bean(s)."))
        }
    }

    @Command("balancetop", "baltop")
    @CommandPermission(Permissions.ECO)
    fun balanceTop(
        sender: CommandSender,
        @Optional
        @Range(min = 1.0)
        page: Int?
    ) {
        for ((username, balance) in DbPlayer.top(page ?: 1)) {
            sender.sendMessage(Component.text("$username -> $balance"))
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
        val s = DbPlayer.lookup(sender) ?: return
        val p = DbPlayer.lookup(player)
            ?: run {
                sender.sendMessage(Component.text("Cannot pay a player who has never joined."))
                return
            }
        if (s.pay(p, amount)) {
            sender.sendMessage(Component.text("You paid $amount bean(s) to ${p.username}."))
        } else {
            sender.sendMessage(Component.text("Could not complete the transaction."))
        }
    }

    private fun economyHelper(sender: CommandSender, player: OfflinePlayer, action: (DbPlayer) -> Boolean) {
        val p = DbPlayer.lookup(player)
            ?: run {
                sender.sendMessage(Component.text("Player argument needed in this context."))
                return
            }
        if (action(p)) {
            sender.sendMessage(Component.text("The balance was updated"))
        } else {
            sender.sendMessage(Component.text("The balance would overflow, so it was not updated."))
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
