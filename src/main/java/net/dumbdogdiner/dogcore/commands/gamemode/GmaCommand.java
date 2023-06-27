package net.dumbdogdiner.dogcore.commands.gamemode;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("gma")
@Permission("minecraft.gamemode")
public final class GmaCommand {
    private GmaCommand() { }

    @Default
    public static void gma(final @NotNull Player player) {
        player.performCommand("gamemode adventure");
    }
}
