package net.dumbdogdiner.dogcore.commands

import kotlinx.coroutines.runBlocking
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command

object TellCommand {
    @Command("tell")
    fun tell(sender: CommandSender, receiver: Player, message: String) {
        // check if muted
        val senderName = if (sender is Player) {
            if (DbPlayer.lookup(sender)?.isMuted == true) {
                sender.server.broadcast(Component.text("hewwo???"))
                sender.sendMessage(Component.text("You are muted."))
            }
            runBlocking { NameFormatter.formatUsername(sender) }
        } else {
            sender.name()
        }

        val unsignedMessage = Component.text(message)

        receiver.sendMessage(
            Component.textOfChildren(
                Component.text("(PM) ", NamedTextColor.RED),
                senderName,
                Component.text(" » ", NamedTextColor.DARK_GRAY),
                unsignedMessage
            )
        )
    }
}
