package net.dumbdogdiner.dogcore.commands;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class EconomyCommands {
    private EconomyCommands() { }

    /**
     * The /balance command.
     * @param sender The sender of the command.
     * @param player The player to check the balance of.
     */
    @Command({"balance", "bal"})
    @CommandPermission(Permissions.ECO)
    public static void balance(final CommandSender sender, @Optional final OfflinePlayer player) {
        OfflinePlayer playerToCheck;
        if (player == null) {
            if (sender instanceof Player p) {
                playerToCheck = p;
            } else {
                sender.sendMessage(Messages.get("error.playerNeeded"));
                return;
            }
        } else {
            playerToCheck = player;
        }

        User.lookupCommand(playerToCheck, sender, user -> user.getBalance().thenAccept(balance -> {
            if (sender.equals(playerToCheck)) {
                sender.sendMessage(Messages.get("commands.balance.query", Component.text(balance)));
            } else {
                user.formattedName().thenAccept(name ->
                    sender.sendMessage(Messages.get("commands.balance.query.other", name, Component.text(balance))));
            }
        }));
    }

    /**
     * The /balancetop command.
     * @param sender The sender of the command.
     * @param page The page to check.
     */
    @Command({"balancetop", "baltop"})
    @CommandPermission(Permissions.ECO)
    public static void balanceTop(final CommandSender sender, @Default("1") @Range(min = 1.0) final int page) {
        User.top(page).thenAccept(entries -> {
            for (var entry : entries) {
                sender.sendMessage(
                    Messages.get("commands.balancetop.entry", entry.name(), Component.text(entry.amount())));
            }
        });
    }

    /**
     * The /pay command.
     * @param sender The sender of the command.
     * @param player The player to send money to.
     * @param amount The amount of money to send.
     */
    @Command("pay")
    @CommandPermission(Permissions.ECO)
    public static void pay(
        final Player sender,
        final OfflinePlayer player,
        @Range(min = 1.0) final long amount
    ) {
        User.lookupCommand(sender, sender,
            from -> User.lookupCommand(player, sender, to -> from.pay(to, amount).thenAccept(success -> {
                if (success) {
                    to.formattedName().thenAccept(name ->
                        sender.sendMessage(Messages.get("commands.pay.success", Component.text(amount), name)));
                } else {
                    sender.sendMessage(Messages.get("error.failedTransaction"));
                }
            })));
    }

    private static void economyHelper(
        @NotNull final CommandSender sender,
        @NotNull final OfflinePlayer player,
        @NotNull final Function<@NotNull User, @NotNull CompletionStage<@NotNull Boolean>> action
    ) {
        User.lookupCommand(player, sender, user -> action.apply(user).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.get("commands.economy.success"));
            } else {
                sender.sendMessage(Messages.get("error.failedTransaction"));
            }
        }));
    }

    @Command({"economy", "eco"})
    @Subcommand("give")
    @CommandPermission(Permissions.ECO_ADMIN)
    public static void economyGive(
        final CommandSender sender,
        final OfflinePlayer player,
        @Range(min = 1.0) final long amount
    ) {
        economyHelper(sender, player, user -> user.give(amount));
    }

    @Command({"economy", "eco"})
    @Subcommand("take")
    @CommandPermission(Permissions.ECO_ADMIN)
    public static void economyTake(
        final CommandSender sender,
        final OfflinePlayer player,
        @Range(min = 1.0) final long amount
    ) {
        economyHelper(sender, player, user -> user.give(-amount));
    }

    @Command({"economy", "eco"})
    @Subcommand("set")
    @CommandPermission(Permissions.ECO_ADMIN)
    public static void economySet(
        final CommandSender sender,
        final OfflinePlayer player,
        final long amount
    ) {
        economyHelper(sender, player, user -> user.setBalance(amount).thenApply(v -> true));
    }
}
