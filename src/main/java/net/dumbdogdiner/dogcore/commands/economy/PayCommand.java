package net.dumbdogdiner.dogcore.commands.economy;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("pay")
@Permission(Permissions.ECO)
public final class PayCommand {
    private PayCommand() { }

    @Default
    public static void pay(
            final @NotNull Player sender,
            final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
            final @ALongArgument(min = 1L) long amount
    ) {
        User.lookupCommand(sender, sender, from -> User.lookupCommand(player, sender, to -> {
            var future = from.pay(to, amount);
            future.thenAccept(success -> {
                if (success) {
                    to.formattedName().thenAccept(name -> {
                        var message = Messages.get(
                            "commands.pay.success",
                            MiscFormatter.formatCurrency(amount),
                            name
                        );
                        sender.sendMessage(message);
                    });
                } else {
                    sender.sendMessage(Messages.get("error.failedTransaction"));
                }
            });
        }));
    }
}
