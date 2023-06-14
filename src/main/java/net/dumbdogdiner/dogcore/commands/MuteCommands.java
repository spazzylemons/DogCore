package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import java.time.Duration;

public final class MuteCommands {
    private MuteCommands() {}

    @Command("mute")
    @CommandPermission(Permissions.MUTE)
    public static void mute(CommandSender sender, OfflinePlayer player, @Optional Duration duration) {
        var user = User.lookupCommand(player);
        user.mute(duration);
        if (duration != null) {
            sender.sendMessage(Messages.get("commands.mute.duration", user.formattedName().join(), Component.text(duration.toString())));
        } else {
            sender.sendMessage(Messages.get("commands.mute.indefinite", user.formattedName().join()));
        }
    }

    @Command("unmute")
    @CommandPermission(Permissions.MUTE)
    public static void unmute(CommandSender sender, OfflinePlayer player) {
        var user = User.lookupCommand(player);
        user.unmute();
        sender.sendMessage(Messages.get("commands.unmute.success", user.formattedName().join()));
    }
}
