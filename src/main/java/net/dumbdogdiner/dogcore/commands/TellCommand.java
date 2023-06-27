package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import java.util.Collection;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
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
        final @NotNull @AEntitySelectorArgument.ManyPlayers Collection<Player> players,
        final @NotNull @AGreedyStringArgument String message
    ) {
        User.nameIfNotMuted(sender).thenAccept(name -> {
            if (name != null) {
                var receiverName = MiscFormatter.playerCollection(players);

                var messageComponent = Component.text(message);
                sender.sendMessage(Messages.get("chat.tell.outgoing", receiverName, messageComponent));
                for (var player : players) {
                    player.sendMessage(Messages.get("chat.tell.incoming", name, messageComponent));
                }

                User.spies().thenAccept(spies -> {
                    if (!spies.isEmpty()) {
                        var spyMessage = Messages.get("chat.tell.spy", name, receiverName, messageComponent);

                        for (var spy : spies) {
                            spy.sendMessage(spyMessage);
                        }
                    }
                });
            }
        });
    }
}
