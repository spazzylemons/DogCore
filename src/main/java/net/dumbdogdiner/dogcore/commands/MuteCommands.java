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
        User.lookupCommand(player, sender).thenApply(user -> user.mute(duration).thenAccept(v -> user.formattedName().thenAccept(name -> {
            if (duration != null) {
                sender.sendMessage(Messages.get("commands.mute.duration", name, Component.text(duration.toString())));
            } else {
                sender.sendMessage(Messages.get("commands.mute.indefinite", name));
            }
        })));
    }

    @Command("unmute")
    @CommandPermission(Permissions.MUTE)
    public static void unmute(CommandSender sender, OfflinePlayer player) {
        User.lookupCommand(player, sender).thenAccept(user -> user.unmute().thenAccept(v ->
            sender.sendMessage(Messages.get("commands.unmute.success", user.formattedName().join()))));
    }
}
