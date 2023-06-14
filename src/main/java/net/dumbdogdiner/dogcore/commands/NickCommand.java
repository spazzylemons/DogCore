package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class NickCommand {
    private NickCommand() {}

    @Command("nick")
    @Subcommand("set")
    @CommandPermission(Permissions.NICK)
    public static void nickSet(Player sender, String nickname) {
        nickHelper(sender, sender, nickname);
    }

    @Command("nick")
    @Subcommand("clear")
    @CommandPermission(Permissions.NICK)
    public static void nickClear(Player sender) {
        nickHelper(sender, sender, null);
    }

    @Command("nick")
    @Subcommand("set-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickSetOther(Player sender, Player player, String nickname) {
        nickHelper(sender, player, nickname);
    }

    @Command("nick")
    @Subcommand("clear-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickClearOther(Player sender, Player player) {
        nickHelper(sender, player, null);
    }

    private static void nickHelper(Player sender, Player player, @Nullable String nickname) {
        User.lookupCommand(player, sender).thenAccept(user -> user.setNickname(nickname).thenAccept(success -> {
            if (success) {
                sender.sendMessage(Messages.get("commands.nick.success"));
            } else {
                sender.sendMessage(Messages.get("commands.nick.tooLong"));
            }
        }));
    }
}
