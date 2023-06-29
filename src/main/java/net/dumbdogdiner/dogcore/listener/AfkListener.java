package net.dumbdogdiner.dogcore.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class AfkListener implements Listener {
    /** The AFK command itself. Used to avoid clearing AFK as a side effect of running /afk. */
    private static final String AFK_COMMAND = "/afk";


    @EventHandler
    public void onJoin(final @NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        // add the player to the system
        AfkManager.addPlayer(player);
    }

    @EventHandler
    public void onQuit(final @NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        // remove the player from the system
        AfkManager.removePlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(final @NotNull PlayerMoveEvent event) {
        var player = event.getPlayer();
        AfkManager.playerMoved(player, event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(final @NotNull AsyncChatEvent event) {
        AfkManager.setAfk(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(final @NotNull PlayerCommandPreprocessEvent event) {
        if (!AFK_COMMAND.equals(event.getMessage())) {
            AfkManager.setAfk(event.getPlayer(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            AfkManager.setAfk(event.getPlayer(), false);
        }
    }
}
