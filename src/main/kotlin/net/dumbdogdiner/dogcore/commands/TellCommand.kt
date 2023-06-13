package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.database.User
import net.dumbdogdiner.dogcore.messages.Messages
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command

object TellCommand {
    @Command("tell", "msg", "w", "whisper", "pm", "t")
    fun tell(sender: CommandSender, receiver: Player, message: String) {
        // check if muted
        val senderName = if (sender is Player) {
            if (User.lookup(sender)?.isMuted == true) {
                commandError(Messages["error.muted"])
            }
            NameFormatter.formatUsername(sender).get()
        } else {
            sender.name()
        }
        val receiverName = NameFormatter.formatUsername(receiver).get()

        val messageComponent = Component.text(message)
        sender.sendMessage(Messages["chat.tell.outgoing", receiverName, messageComponent])
        receiver.sendMessage(Messages["chat.tell.incoming", senderName, messageComponent])

        val spies = User.spies()
        if (spies.isNotEmpty()) {
            val spyMessage = Messages["chat.tell.spy", senderName, receiverName, messageComponent]

            for (spy in spies) {
                spy.sendMessage(spyMessage)
            }
        }
    }
}
