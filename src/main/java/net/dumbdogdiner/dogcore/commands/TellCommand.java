package net.dumbdogdiner.dogcore.commands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
        CompletionStage<Boolean> isMutedFuture = CompletableFuture.completedFuture(false);
        if (sender instanceof Player p) {
            isMutedFuture = User.lookupCommand(p, sender).thenCompose(User::isMuted);
        }
        isMutedFuture.thenAccept(isMuted -> {
            if (isMuted) {
                sender.sendMessage(Messages.get("error.muted"));
                return;
            }

            Component senderName;
            if (sender instanceof Player p) {
                senderName = p.displayName();
            } else {
                senderName = sender.name();
            }

            var receiverName = player.displayName();

            var messageComponent = Component.text(message);
            sender.sendMessage(Messages.get("chat.tell.outgoing", receiverName, messageComponent));
            player.sendMessage(Messages.get("chat.tell.incoming", senderName, messageComponent));

            User.spies().thenAccept(spies -> {
                if (!spies.isEmpty()) {
                    var spyMessage = Messages.get("chat.tell.spy", senderName, receiverName, messageComponent);

                    for (var spy : spies) {
                        spy.sendMessage(spyMessage);
                    }
                }
            });
        });
    }
}
