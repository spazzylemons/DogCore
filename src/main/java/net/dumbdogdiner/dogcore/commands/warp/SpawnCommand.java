package net.dumbdogdiner.dogcore.commands.warp;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("spawn")
@Permission(Permissions.WARP)
public final class SpawnCommand {
    private SpawnCommand() { }

    @Default
    public static void spawn(final @NotNull Player player) {
        player.sendMessage(Messages.get("commands.spawn"));
        TeleportHelper.safeTeleport(player, TeleportHelper.getSpawnLocation());
    }
}
