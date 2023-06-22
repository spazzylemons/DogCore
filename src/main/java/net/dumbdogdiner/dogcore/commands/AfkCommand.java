package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("afk")
public final class AfkCommand {
    private AfkCommand() { }

    @Default
    public static void afk(final @NotNull Player player) {
        AfkManager.toggleAfk(player);
    }
}
