package net.dumbdogdiner.dogcore.commands.mute;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command("unmute")
@Permission(Permissions.MUTE)
public final class UnmuteCommand {
    private UnmuteCommand() { }

    @Default
    public static void unmute(
        final @NotNull CommandSender sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player
    ) {
        User.lookupCommand(player, sender, user -> user.unmute().thenAccept(v ->
            sender.sendMessage(Messages.get("commands.unmute.success", user.formattedName().join()))));
    }
}
