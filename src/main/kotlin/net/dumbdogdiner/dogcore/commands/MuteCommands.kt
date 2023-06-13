package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.User
import net.dumbdogdiner.dogcore.messages.Messages
import net.dumbdogdiner.dogcore.util.CoroutineThreadPool
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission
import kotlin.time.Duration

object MuteCommands {
    @Command("mute")
    @CommandPermission(Permissions.MUTE)
    fun mute(
        sender: CommandSender,
        player: OfflinePlayer,
        @Optional
        duration: Duration?
    ) {
        CoroutineThreadPool.launch {
            val user = User.lookupCommand(player)
            user.mute(duration)
            if (duration != null) {
                sender.sendMessage(Messages["commands.mute.duration", user.formattedName(), Component.text(duration.toString())])
            } else {
                sender.sendMessage(Messages["commands.mute.indefinite", user.formattedName()])
            }
        }
    }

    @Command("unmute")
    @CommandPermission(Permissions.MUTE)
    fun unmute(sender: CommandSender, player: OfflinePlayer) {
        CoroutineThreadPool.launch {
            val user = User.lookupCommand(player)
            user.unmute()
            sender.sendMessage(Messages["commands.unmute.success", user.formattedName()])
        }
    }
}
