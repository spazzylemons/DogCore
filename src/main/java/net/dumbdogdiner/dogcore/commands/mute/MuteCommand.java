package net.dumbdogdiner.dogcore.commands.mute;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AOfflinePlayerArgument;
import dev.jorel.commandapi.annotations.arguments.ATimeArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import org.jetbrains.annotations.Nullable;

@Command("mute")
@Permission(Permissions.MUTE)
public final class MuteCommand {
    /** Number of milliseconds per tick. */
    private static final long MILLISECONDS_PER_TICK = 50L;

    private MuteCommand() { }

    @Default
    public static void mute(
        final @NotNull CommandSender sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player,
        final @ATimeArgument int ticks
    ) {
        muteHelper(sender, player, Duration.ofMillis(ticks * MILLISECONDS_PER_TICK));
    }

    @Default
    public static void mute(
        final @NotNull CommandSender sender,
        final @NotNull @AOfflinePlayerArgument OfflinePlayer player
    ) {
        muteHelper(sender, player, null);
    }

    private static void muteHelper(
        final @NotNull CommandSender sender,
        final @NotNull OfflinePlayer player,
        final @Nullable Duration duration
    ) {
        User.lookupCommand(player, sender,
            user -> user.mute(duration).thenAccept(v -> user.formattedName().thenAccept(name -> {
                if (duration != null) {
                    var durationText = Component.text(duration.toString());
                    sender.sendMessage(Messages.get("commands.mute.duration", name, durationText));
                } else {
                    sender.sendMessage(Messages.get("commands.mute.indefinite", name));
                }
            })));
    }
}
