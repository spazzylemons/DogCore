package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.teleport.TpaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class TpaListener implements Listener {
    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        TpaManager.removePlayer(event.getPlayer().getUniqueId());
    }
}
