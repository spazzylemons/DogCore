package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class NickCommand {
    private NickCommand() { }

    @Command("nick")
    @Subcommand("set")
    @CommandPermission(Permissions.NICK)
    public static void nickSet(final Player sender, final String nickname) {
        nickHelper(sender, sender, nickname);
    }

    @Command("nick")
    @Subcommand("clear")
    @CommandPermission(Permissions.NICK)
    public static void nickClear(final Player sender) {
        nickHelper(sender, sender, null);
    }

    @Command("nick")
    @Subcommand("set-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickSetOther(final Player sender, final Player player, final String nickname) {
        nickHelper(sender, player, nickname);
    }

    @Command("nick")
    @Subcommand("clear-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickClearOther(final Player sender, final Player player) {
        nickHelper(sender, player, null);
    }

    private static void nickHelper(
        @NotNull final Player sender,
        @NotNull final Player player,
        @Nullable final String nickname
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
