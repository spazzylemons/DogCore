package net.dumbdogdiner.dogcore.commands.economy;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command("baltop")
@Alias("balancetop")
@Permission(Permissions.ECO)
public final class BalanceTopCommand {
    private BalanceTopCommand() { }

    @Default
    public static void balTop(final @NotNull CommandSender sender) {
        balTop(sender, 1);
    }

    @Default
    public static void balTop(
        final @NotNull CommandSender sender,
        final @AIntegerArgument(min = 1) int page
    ) {
        User.top(page).thenAccept(entries -> {
            for (var entry : entries) {
                sender.sendMessage(
                    Messages.get(
                        "commands.balancetop.entry",
                        entry.name(),
                        MiscFormatter.formatCurrency(entry.amount())
                    ));
            }
        });
    }
}
