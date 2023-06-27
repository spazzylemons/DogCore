package net.dumbdogdiner.dogcore.commands.gamemode;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("gmsp")
@Permission("minecraft.gamemode")
public final class GmspCommand {
    private GmspCommand() { }

    @Default
    public static void gmsp(final @NotNull Player player) {
        player.performCommand("gamemode spectator");
    }
}
