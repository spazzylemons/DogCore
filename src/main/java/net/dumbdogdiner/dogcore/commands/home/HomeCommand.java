package net.dumbdogdiner.dogcore.commands.home;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.HomeManager;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("home")
public final class HomeCommand {
    private HomeCommand() { }

    @Default
    public static void home(final @NotNull Player sender) {
        var home = HomeManager.getHome(sender);
        if (home != null) {
            TeleportHelper.safeTeleport(sender, home);
            sender.sendMessage(Messages.get("commands.home.success"));
        } else {
            sender.sendMessage(Messages.get("commands.home.noHome"));
        }
    }
}
