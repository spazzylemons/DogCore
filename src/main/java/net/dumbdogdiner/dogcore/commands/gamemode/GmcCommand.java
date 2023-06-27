package net.dumbdogdiner.dogcore.commands.gamemode;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("gmc")
@Permission("minecraft.gamemode")
public final class GmcCommand {
    private GmcCommand() { }

    @Default
    public static void gmc(final @NotNull Player player) {
        player.performCommand("gamemode creative");
    }
}
