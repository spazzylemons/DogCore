package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class SpawnCommand {
    private SpawnCommand() { }

    @Command("spawn")
    @CommandPermission(Permissions.WARP)
    public static void warp(final Player player) {
        player.sendMessage(Messages.get("commands.spawn"));
        TeleportHelper.safeTeleport(player, TeleportHelper.getSpawnLocation());
    }
}
