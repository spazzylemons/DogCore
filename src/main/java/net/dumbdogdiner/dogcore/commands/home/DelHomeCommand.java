package net.dumbdogdiner.dogcore.commands.home;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.HomeManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("delhome")
public final class DelHomeCommand {
    private DelHomeCommand() { }

    @Default
    public static void delHome(final @NotNull Player sender) {
        if (HomeManager.delHome(sender)) {
            sender.sendMessage(Messages.get("commands.delhome.success"));
        } else {
            sender.sendMessage(Messages.get("commands.home.noHome"));
        }
    }
}
