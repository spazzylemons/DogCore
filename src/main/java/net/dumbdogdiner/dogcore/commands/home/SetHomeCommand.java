package net.dumbdogdiner.dogcore.commands.home;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.HomeManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("sethome")
@Permission(Permissions.HOME)
public final class SetHomeCommand {
    private SetHomeCommand() { }

    @Default
    public static void setHome(final @NotNull Player sender) {
        HomeManager.setHome(sender, sender.getLocation());
        sender.sendMessage(Messages.get("commands.sethome.success"));
    }
}
