package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class NickCommand {
    private NickCommand() {}

    @Command("nick")
    @Subcommand("set")
    @CommandPermission(Permissions.NICK)
    public static void nickSet(Player sender, String nickname) {
        nickSetOther(sender, sender, nickname);
    }

    @Command("nick")
    @Subcommand("clear")
    @CommandPermission(Permissions.NICK)
    public static void nickClear(Player sender) {
        nickClearOther(sender, sender);
    }

    @Command("nick")
    @Subcommand("set-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickSetOther(Player sender, Player player, String nickname) {
        var user = User.lookupCommand(player);
        if (user.setNickname(nickname)) {
            sender.sendMessage(Messages.get("commands.nick.success"));
        } else {
            sender.sendMessage(Messages.get("commands.nick.tooLong"));
        }
    }

    @Command("nick")
    @Subcommand("clear-other")
    @CommandPermission(Permissions.NICK_ADMIN)
    public static void nickClearOther(Player sender, Player player) {
        var user = User.lookupCommand(player);
        user.setNickname(null);
        sender.sendMessage(Messages.get("commands.nick.success"));
    }
}
