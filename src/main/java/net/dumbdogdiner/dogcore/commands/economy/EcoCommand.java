package net.dumbdogdiner.dogcore.commands.economy;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Command("eco")
@Alias("economy")
@Permission(Permissions.ECO_ADMIN)
public final class EcoCommand {
    private EcoCommand() { }

    private static void economyHelper(
            final @NotNull CommandSender sender,
            final @NotNull OfflinePlayer player,
            final @NotNull Function<@NotNull User, @NotNull CompletionStage<@NotNull Boolean>> action
    ) {
        User.lookupCommand(player, sender, user -> action.apply(user).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.get("commands.economy.success"));
            } else {
                sender.sendMessage(Messages.get("error.failedTransaction"));
            }
        }));
    }

    @Subcommand("give")
    public static void give(
            final @NotNull CommandSender sender,
            final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
            final @ALongArgument(min = 1) long amount
    ) {
        economyHelper(sender, player, user -> user.give(amount));
    }

    @Subcommand("take")
    public static void take(
            final @NotNull CommandSender sender,
            final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
            final @ALongArgument(min = 1) long amount
    ) {
        economyHelper(sender, player, user -> user.give(-amount));
    }

    @Subcommand("set")
    public static void set(
            final @NotNull CommandSender sender,
            final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
            final @ALongArgument(min = 0) long amount
    ) {
        economyHelper(sender, player, user -> user.setBalance(amount));
    }
}
