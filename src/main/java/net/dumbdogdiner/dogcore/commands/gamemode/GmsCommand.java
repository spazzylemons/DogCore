package net.dumbdogdiner.dogcore.commands.gamemode;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("gms")
@Permission("minecraft.gamemode")
public final class GmsCommand {
    private GmsCommand() { }

    @Default
    public static void gms(final @NotNull Player player) {
        player.performCommand("gamemode survival");
    }
}
