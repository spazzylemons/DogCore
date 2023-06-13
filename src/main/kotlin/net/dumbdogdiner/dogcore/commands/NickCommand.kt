package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.User
import net.dumbdogdiner.dogcore.db.tables.Users
import net.dumbdogdiner.dogcore.messages.Messages
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

object NickCommand {
    @Command("nick")
    @Subcommand("set")
    @CommandPermission(Permissions.NICK)
    fun nickSet(sender: Player, nickname: String) {
        val user = User.lookupCommand(sender)
        if (nickname.length > Users.MAX_NICKNAME_LENGTH) {
            sender.sendMessage(Messages["commands.nick.tooLong"])
        } else {
            user.nickname = nickname
            sender.sendMessage(Messages["commands.nick.success"])
        }
    }

    @Command("nick")
    @Subcommand("clear")
    @CommandPermission(Permissions.NICK)
    fun nickClear(sender: Player) {
        val user = User.lookupCommand(sender)
        user.nickname = null
        sender.sendMessage(Messages["commands.nick.success"])
    }

    @Command("nick")
    @Subcommand("set-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    fun nickSetOther(sender: Player, player: Player, nickname: String) {
        val user = User.lookupCommand(player)
        if (nickname.length > Users.MAX_NICKNAME_LENGTH) {
            sender.sendMessage(Messages["commands.nick.tooLong"])
        } else {
            user.nickname = nickname
            sender.sendMessage(Messages["commands.nick.success"])
        }
    }

    @Command("nick")
    @Subcommand("clear-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    fun nickClearOther(sender: Player, player: Player) {
        val user = User.lookupCommand(player)
        user.nickname = null
        sender.sendMessage(Messages["commands.nick.success"])
    }
}
