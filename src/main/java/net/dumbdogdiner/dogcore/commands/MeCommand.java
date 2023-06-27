package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command("me")
@Permission(Permissions.ME)
public final class MeCommand {
    private MeCommand() { }

    @Default
    public static void me(
        final @NotNull CommandSender sender,
        final @NotNull @AGreedyStringArgument String message
    ) {
        User.nameIfNotMuted(sender).thenAccept(name -> {
            if (name != null) {
                Bukkit.broadcast(Component.textOfChildren(name, Component.space(), Component.text(message)));
            }
        });
    }
}
