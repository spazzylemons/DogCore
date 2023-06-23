package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command("dogcore-reload")
@Permission(Permissions.RELOAD)
public final class ReloadCommand {
    private ReloadCommand() { }

    @Default
    public static void reload(final @NotNull CommandSender sender) {
        sender.sendMessage(Messages.get("commands.reload"));
        Configuration.load();
    }
}
