package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("tell")
@Alias({"msg", "w", "whisper", "pm", "t"})
public final class TellCommand {
    private TellCommand() { }

    @Default
    public static void tell(
        final @NotNull CommandSender sender,
        final @NotNull @APlayerArgument Player player,
        final @NotNull @AGreedyStringArgument String message
    ) {
        // check if muted
        CompletionStage<Boolean> isMutedFuture;
        if (sender instanceof Player p) {
            var future = new CompletableFuture<Boolean>();
            isMutedFuture = future;
            User.lookupCommand(
                p,
                sender,
                user -> user.isMuted().thenAccept(future::complete),
                () -> future.complete(false)
            );
        } else {
            isMutedFuture = CompletableFuture.completedFuture(false);
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
