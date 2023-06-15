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
    private AfkManager() { }

    /** The time it takes to turn AFK. */
    public static final long AFK_MS = 300L * 1000L;

    /** The distance squared needed to move to not be marked AFK. */
    public static final double MIN_DIST_SQUARED = 16.0;

    /** Ticks in a second. */
    private static final int TPS = 20;

    private record AfkCancelEvent<T extends PlayerEvent>(
        @NotNull Class<T> clazz,
        @NotNull Predicate<T> condition
    ) implements EventExecutor, Listener {
        @Override
        @SuppressWarnings("unchecked")
        public void execute(@NotNull final Listener listener, @NotNull final Event event) {
            var playerEvent = (T) event;
            if (condition().test(playerEvent)) {
                insert(playerEvent.getPlayer());
            }
        }

        public void register(@NotNull final JavaPlugin plugin) {
            Bukkit.getPluginManager().registerEvent(clazz, this, EventPriority.HIGHEST, this, plugin);
        }
    }

    /** The events to cancel AFK on. */
    @SuppressWarnings("unchecked")
    private static final AfkCancelEvent<? extends PlayerEvent>[] EVENTS = new AfkCancelEvent[]{
        new AfkCancelEvent<>(AsyncChatEvent.class, e -> true),
        new AfkCancelEvent<>(PlayerCommandPreprocessEvent.class, e -> true),
        new AfkCancelEvent<>(PlayerInteractEvent.class, e -> e.getAction() != Action.PHYSICAL),
    };

    private record Entry(@NotNull UUID player, long time) { }

    /**
     * A list of all players that are not AFK. The players at the head of the list have moved least recently.
     */
    private static final LinkedQueue<Entry> QUEUE = new LinkedQueue<>();

    /** A map of players to entry nodes. */
    private static final Map<@NotNull UUID, LinkedQueue.@NotNull Node<Entry>> PLAYERS = new HashMap<>();

    /** A map of players to entry nodes. */
    private static final Map<@NotNull UUID, @NotNull Location> AFK_LOCATIONS = new HashMap<>();

    public static void init(@NotNull final JavaPlugin plugin) {
        // establish a repeating task to query the player list
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, AfkManager::setAfkPlayers, 0, TPS);
        // register event handlers for each event that will cause a player to be no longer AFK
        for (var event : EVENTS) {
            event.register(plugin);
        }
    }

    public static synchronized boolean isAfk(@NotNull final UUID uuid) {
        return !PLAYERS.containsKey(uuid);
    }

    public static synchronized void insert(
        @NotNull final UUID uuid,
        @NotNull final Location newLocation
    ) {
        PLAYERS.put(uuid, QUEUE.push(new Entry(uuid, System.currentTimeMillis())));
        AFK_LOCATIONS.put(uuid, newLocation);
    }

    public static void insert(@NotNull final Player player) {
        insert(player.getUniqueId(), player.getLocation());
    }

    public static synchronized boolean remove(@NotNull final UUID uuid) {
        var oldNode = PLAYERS.remove(uuid);
        if (oldNode != null) {
            QUEUE.remove(oldNode);
            return true;
        }
        return false;
    }

    /**
     * Remove a player's data from the AFK manager.
     * @param uuid The player's unique ID.
     */
    public static synchronized void removeLocation(@NotNull final UUID uuid) {
        AFK_LOCATIONS.remove(uuid);
    }

    /**
     * Potentially clear a player's AFK state.
     * @param uuid The player's unique ID.
     * @param newLocation The new location of the player.
     * @param force If true, this will always clear AFK state.
     */
    public static synchronized void clearAfk(
        @NotNull final UUID uuid,
        @NotNull final Location newLocation,
        final boolean force
    ) {
        // is the location significantly far enough?
        var afkLocation = AFK_LOCATIONS.get(uuid);
        if (afkLocation != null) {
            if (!force
                && afkLocation.getWorld().equals(newLocation.getWorld())
                && afkLocation.distanceSquared(newLocation) < MIN_DIST_SQUARED) {
                // nothing to change
                return;
            } else {
                // load new AFK location
                AFK_LOCATIONS.put(uuid, afkLocation);
            }
        }
        var wasAfk = isAfk(uuid);
        remove(uuid);
        insert(uuid, newLocation);
        if (wasAfk && !isAfk(uuid)) {
            announceAfk(uuid);
        }
    }

    /**
     * Toggle the AFK status of a player.
     * @param uuid The player's unique ID.
     * @param playerLocation The location of the player.
     */
    public static synchronized void toggleAfk(
        @NotNull final UUID uuid,
        @NotNull final Location playerLocation
    ) {
        if (!remove(uuid)) {
            insert(uuid, playerLocation);
        }
        announceAfk(uuid);
    }

    /**
     * Announce a player's AFK state.
     * @param uuid The player's unique ID.
     */
    private static synchronized void announceAfk(@NotNull final UUID uuid) {
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

    /**
     * Check players and set AFK players as necessary.
     */
    private static synchronized void setAfkPlayers() {
        var now = System.currentTimeMillis();
        while (true) {
            var node = QUEUE.peek();
            if (node != null && node.getValue().time + AFK_MS < now) {
                var player = node.getValue().player;
                PLAYERS.remove(player);
                QUEUE.remove(node);
                announceAfk(player);
                continue;
            }
            return;
        }
    }
}
