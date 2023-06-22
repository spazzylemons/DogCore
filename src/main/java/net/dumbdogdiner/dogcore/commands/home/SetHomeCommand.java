package net.dumbdogdiner.dogcore.commands.home;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.HomeManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("sethome")
public final class SetHomeCommand {
    private SetHomeCommand() { }

    @Default
    public static void setHome(final @NotNull Player sender) {
        HomeManager.setHome(sender, sender.getLocation());
        sender.sendMessage(Messages.get("commands.sethome.success"));
    }
}
