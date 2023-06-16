package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.afk.AfkManager;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public final class AfkCommand {
    private AfkCommand() { }

    /**
     * The /afk command.
     * @param player The player sending the command.
     */
    @Command("afk")
    public static void afk(final Player player) {
        AfkManager.toggleAfk(player);
    }
}
