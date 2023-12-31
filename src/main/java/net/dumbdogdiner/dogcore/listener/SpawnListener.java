package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.teleport.HomeManager;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class SpawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(final @NotNull PlayerRespawnEvent event) {
        var player = event.getPlayer();
        var location = HomeManager.getHome(player);
        if (location != null) {
            // find safe location by home
            event.setRespawnLocation(TeleportHelper.getSafeTeleport(location));
        } else if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            // if the player has a bed or anchor, use that, otherwise use exact spawn point
            // no random offset like in vanilla!
            event.setRespawnLocation(TeleportHelper.getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawnLocation(final @NotNull PlayerSpawnLocationEvent event) {
        // if player hasn't played before, use the exact spawn point
        var player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            event.setSpawnLocation(TeleportHelper.getSpawnLocation());
        }
    }
}
