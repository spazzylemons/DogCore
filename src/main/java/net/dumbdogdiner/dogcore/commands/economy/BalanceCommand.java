package net.dumbdogdiner.dogcore.commands.economy;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("balance")
@Alias("bal")
@Permission(Permissions.ECO)
public final class BalanceCommand {
    private BalanceCommand() { }

    @Default
    public static void balance(final @NotNull Player sender) {
        User.lookupCommand(sender, sender, user -> user.getBalance().thenAccept(balance ->
            sender.sendMessage(Messages.get("commands.balance.query", MiscFormatter.formatCurrency(balance)))));
    }

    @Default
    public static void balance(
        final @NotNull CommandSender sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player
    ) {
        User.lookupCommand(player, sender, user -> user.getBalance().thenAccept(balance ->
            user.formattedName().thenAccept(name ->
                sender.sendMessage(Messages.get(
                    "commands.balance.query.other",
                    name,
                    MiscFormatter.formatCurrency(balance)
                )))));
    }
}
