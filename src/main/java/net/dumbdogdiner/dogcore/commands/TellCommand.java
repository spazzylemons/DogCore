package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public final class TellCommand {
    private TellCommand() {}

    @Command({"tell", "msg", "w", "whisper", "pm", "t"})
    public static void tell(CommandSender sender, Player player, String message) {
        // check if muted
        Component senderName;
        if (sender instanceof Player p) {
            if (User.lookupCommand(p).isMuted()) {
                throw new FormattedCommandException(Messages.INSTANCE.get("error.muted"));
            }
            senderName = NameFormatter.formatUsername(p).join();
        } else {
            senderName = sender.name();
        }
        var receiverName = NameFormatter.formatUsername(player).join();

        var messageComponent = Component.text(message);
        sender.sendMessage(Messages.INSTANCE.get("chat.tell.outgoing", receiverName, messageComponent));
        player.sendMessage(Messages.INSTANCE.get("chat.tell.incoming", senderName, messageComponent));

        var spies = User.spies();
        if (!spies.isEmpty()) {
            var spyMessage = Messages.INSTANCE.get("chat.tell.spy", senderName, receiverName, messageComponent);

            for (var spy : spies) {
                spy.sendMessage(spyMessage);
            }
        }
    }
}
