package net.dumbdogdiner.dogcore.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.util.LinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class AfkManager {
    public static final long AFK_MS = 300L * 1000L;

    public static final double MIN_DIST_SQUARED = 16.0;

    private record AfkCancelEvent<T extends PlayerEvent>(Class<T> clazz, Predicate<T> condition) implements EventExecutor, Listener {
        @Override
        @SuppressWarnings("unchecked")
        public void execute(@NotNull Listener listener, @NotNull Event event) {
            var playerEvent = (T) event;
            if (condition().test(playerEvent)) {
                insert(playerEvent.getPlayer());
            }
        }

        public void register(@NotNull JavaPlugin plugin) {
            Bukkit.getPluginManager().registerEvent(clazz, this, EventPriority.HIGHEST, this, plugin);
        }
    }

    @SuppressWarnings("unchecked")
    private static final AfkCancelEvent<? extends PlayerEvent>[] EVENTS = new AfkCancelEvent[]{
        new AfkCancelEvent<>(AsyncChatEvent.class, e -> true),
        new AfkCancelEvent<>(PlayerCommandPreprocessEvent.class, e -> true),
        new AfkCancelEvent<>(PlayerInteractEvent.class, e -> e.getAction() != Action.PHYSICAL),
    };

    private record Entry(@NotNull UUID player, long time) {}

    /**
     * A list of all players that are not AFK. The players at the head of the list have moved least recently.
     */
    private static final LinkedQueue<Entry> queue = new LinkedQueue<>();

    /** A map of players to entry nodes. */
    private static final Map<@NotNull UUID, LinkedQueue.@NotNull Node<Entry>> players = new HashMap<>();

    /** A map of players to entry nodes. */
    private static final Map<@NotNull UUID, @NotNull Location> afkLocations = new HashMap<>();

    public static void init(@NotNull JavaPlugin plugin) {
        // establish a repeating task to query the player list
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, AfkManager::setAfkPlayers, 20, 20);
        // register event handlers for each event that will cause a player to be no longer AFK
        for (var event : EVENTS) {
            event.register(plugin);
        }
    }

    public static synchronized boolean isAfk(@NotNull UUID uuid) {
        return !players.containsKey(uuid);
    }

    public static synchronized void insert(@NotNull UUID uuid, @NotNull Location newLocation) {
        players.put(uuid, queue.push(new Entry(uuid, System.currentTimeMillis())));
        afkLocations.put(uuid, newLocation);
    }

    public static void insert(@NotNull Player player) {
        insert(player.getUniqueId(), player.getLocation());
    }

    public static synchronized boolean remove(@NotNull UUID uuid) {
        var oldNode = players.remove(uuid);
        if (oldNode != null) {
            queue.remove(oldNode);
            return true;
        }
        return false;
    }

    public static synchronized void removeLocation(@NotNull UUID uuid) {
        afkLocations.remove(uuid);
    }

    public static synchronized void clearAfk(@NotNull UUID uuid, @NotNull Location newLocation, boolean force) {
        // is the location significantly far enough?
        var afkLocation = afkLocations.get(uuid);
        if (afkLocation != null) {
            if (!force && afkLocation.getWorld().equals(newLocation.getWorld()) && afkLocation.distanceSquared(newLocation) < MIN_DIST_SQUARED) {
                // nothing to change
                return;
            } else {
                // load new AFK location
                afkLocations.put(uuid, afkLocation);
            }
        }
        var wasAfk = isAfk(uuid);
        remove(uuid);
        insert(uuid, newLocation);
        if (wasAfk && !isAfk(uuid)) {
            announceAfk(uuid);
        }
    }

    public static synchronized void toggleAfk(@NotNull UUID uuid, @NotNull Location playerLocation) {
        if (!remove(uuid)) {
            insert(uuid, playerLocation);
        }
        announceAfk(uuid);
    }

    private static synchronized void announceAfk(@NotNull UUID uuid) {
        User.lookup(uuid).thenCompose(user -> {
            if (user != null) {
                return user.formattedName().thenAccept(name -> {
                    var message = isAfk(uuid) ? "chat.afk.on" : "chat.afk.off";
                    Bukkit.broadcast(Messages.get(message, name));
                });
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    private static synchronized void setAfkPlayers() {
        var now = System.currentTimeMillis();
        while (true) {
            var node = queue.peek();
            if (node != null && node.getValue().time + AFK_MS < now) {
                var player = node.getValue().player;
                players.remove(player);
                queue.remove(node);
                announceAfk(player);
                continue;
            }
            return;
        }
    }
}
