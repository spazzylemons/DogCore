package net.dumbdogdiner.dogcore.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CoreListener implements Listener {
    /**
     * Formats chat messages.
     * @param event Event to handle.
     */
    @EventHandler
    public void onChat(final AsyncChatEvent event) {
        // this runs on a separate thread, so we aren't afraid to block
        var player = event.getPlayer();
        var user = User.lookup(player).toCompletableFuture().join();
        if (user != null && user.isMuted().toCompletableFuture().join()) {
            // if player is muted, tell them that, and don't send the message
            event.setCancelled(true);
            player.sendMessage(Messages.get("error.muted"));
            return;
        }
        // format message
        event.renderer((source, sourceDisplayName, message, viewer) ->
            Messages.get("chat", source.displayName(), message));
    }

    /**
     * Handles player join.
     * @param event Event to handle.
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        var player = event.getPlayer();
        // register player, if not registered already
        // we only block here to ensure that no other code runs until this user is registered
        var firstJoin = User.register(player).toCompletableFuture().join();
        // set their tab list name
        NameFormatter.refreshPlayerName(player).toCompletableFuture().join();
        // announce join
        var name = player.displayName();
        event.joinMessage(Messages.get("chat.join", name));
        // welcome message if new to server
        if (firstJoin) {
            Bukkit.broadcast(Messages.get("chat.welcome", name));
        }
    }

    /**
     * Handles player quit.
     * @param event Event to handle.
     */
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        var player = event.getPlayer();
        var name = player.displayName();
        event.quitMessage(Messages.get("chat.quit", name));
    }

    /**
     * Handles player death.
     * @param event Event to handle.
     */
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        var player = event.getPlayer();
        var lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause != null) {
            Component attacker;
            if (lastDamageCause instanceof EntityDamageByBlockEvent byBlock) {
                var block = byBlock.getDamager();
                if (block != null) {
                    attacker = Component.translatable(block.getType().translationKey());
                } else {
                    attacker = null;
                }
            } else if (lastDamageCause instanceof EntityDamageByEntityEvent byEntity) {
                var damager = byEntity.getDamager();
                if (damager instanceof Player p) {
                    attacker = p.displayName();
                } else {
                    attacker = damager.name();
                }
            } else {
                attacker = null;
            }
            var cause = lastDamageCause.getCause();
            var name = player.displayName();
            var message = DeathMessageRandomizer.select(cause, name, attacker);
            if (message != null) {
                event.deathMessage(message);
            }
        }

        if (!player.hasPermission(Permissions.BACK)) {
            player.sendMessage(Messages.get("chat.back").clickEvent(ClickEvent.runCommand("/back")));
        }
    }
}
