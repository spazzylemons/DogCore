package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command("nick")
public final class NickCommand {
    private NickCommand() { }

    @Subcommand("set")
    @Permission(Permissions.NICK)
    public static void nickSet(
        final @NotNull Player sender,
        final @NotNull @AStringArgument String nickname
    ) {
        nickHelper(sender, sender, nickname);
    }

    @Subcommand("set")
    @Permission(Permissions.NICK_ADMIN)
    public static void nickSetOther(
        final @NotNull CommandSender sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
        final @NotNull @AStringArgument String nickname
    ) {
        nickHelper(sender, player, nickname);
    }

    @Subcommand("clear")
    @Permission(Permissions.NICK)
    public static void nickClear(final @NotNull Player sender) {
        nickHelper(sender, sender, null);
    }

    @Subcommand("clear")
    @Permission(Permissions.NICK_ADMIN)
    public static void nickClearOther(
        final @NotNull Player sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player
    ) {
        nickHelper(sender, player, null);
    }

    private static void nickHelper(
        final @NotNull CommandSender sender,
        final @NotNull OfflinePlayer player,
        final @Nullable String nickname
    ) {
        User.lookupCommand(player, sender, user  -> user.setNickname(nickname).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.get("commands.nick.success"));
            } else {
                sender.sendMessage(Messages.get("commands.nick.tooLong"));
            }
        }));
    }
}
