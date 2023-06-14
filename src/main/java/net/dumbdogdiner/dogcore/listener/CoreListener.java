package net.dumbdogdiner.dogcore.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.commands.BackCommand;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
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
import org.jetbrains.annotations.NotNull;

public final class CoreListener implements Listener {
    private final @NotNull DogCorePlugin plugin;

    private final @NotNull DeathMessageRandomizer death;

    public CoreListener(@NotNull DogCorePlugin plugin) {
        this.plugin = plugin;
        this.death = new DeathMessageRandomizer(plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();
        var user = User.lookup(player);
        if (user != null && user.isMuted()) {
            // if player is muted, tell them that, and don't send the message
            event.setCancelled(true);
            player.sendMessage(Messages.get("error.muted"));
            return;
        }
        // format message
        event.renderer((source, sourceDisplayName, message, viewer) ->
            Messages.get("chat", NameFormatter.formatUsername(source).join(), message));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        // register player, if not registered already
        var firstJoin = User.register(player);
        // set their tab list name
        var name = NameFormatter.formatUsername(player).join();
        player.displayName(name);
        player.playerListName(name);
        // remove access to some vanilla commands
        plugin.removeVanillaOverrides(player);
        // announce join
        event.joinMessage(Messages.get("chat.join", name));
        // welcome message if new to server
        if (firstJoin) {
            Bukkit.broadcast(Messages.get("chat.welcome", name));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var name = NameFormatter.formatUsername(player).join();
        event.quitMessage(Messages.get("chat.quit", name));
        TpaManager.removePlayer(player.getUniqueId());
        BackCommand.removeBack(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var lastDamageCause = event.getPlayer().getLastDamageCause();
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
                if (damager instanceof Player player) {
                    attacker = NameFormatter.formatUsername(player).join();
                } else {
                    attacker = damager.name();
                }
            } else {
                attacker = null;
            }
            var message = death.select(lastDamageCause.getCause(), NameFormatter.formatUsername(event.getPlayer()).join(), attacker);
            if (message != null) {
                event.deathMessage(message);
            }
        }

        if (!event.getPlayer().hasPermission(Permissions.BACK)) {
            event.getPlayer().sendMessage(Messages.get("chat.back").clickEvent(ClickEvent.runCommand("/back")));
            BackCommand.setBack(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
        }
    }
}
