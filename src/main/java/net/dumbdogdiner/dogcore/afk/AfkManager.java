package net.dumbdogdiner.dogcore.afk;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.event.AfkChangeEvent;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.task.TaskFrequency;
import net.dumbdogdiner.dogcore.task.TaskManager;
import net.dumbdogdiner.dogcore.util.LinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AfkManager {
    private AfkManager() { }

    /** The time it takes to turn AFK. */
    private static long afkMs;

    /** The distance squared needed to move to not be marked AFK. */
    public static final double MIN_DIST_SQUARED = 16.0;

    /** The AFK states for each player. */
    private static final Map<@NotNull Player, @NotNull AfkState> STATES = new HashMap<>();

    /** The queue of non-AFK players. */
    private static LinkedQueue<@NotNull AfkNode> queue;

    static {
        TaskManager.async(TaskFrequency.HIGH, AfkManager::autoAfk);
        // register configurable
        Configuration.register(() -> {
            var time = Duration.ofSeconds(Configuration.getInt("afk.timeout")).toMillis();
            synchronized (AfkManager.class) {
                queue = new LinkedQueue<>();
                for (var entry : STATES.entrySet()) {
                    var value = entry.getValue();
                    // this check prevents us from removing people from their AFK state when we reload config.
                    if (value.timeoutNode() != null) {
                        var node = queue.push(new AfkNode(entry.getKey()));
                        entry.setValue(new AfkState(value.lastLocation(), node));
                    }
                }
                afkMs = time;
            }
        });
    }

    public static synchronized boolean isAfk(final @NotNull Player player) {
        var state = STATES.get(player);
        if (state != null) {
            return state.isAfk();
        }
        return false;
    }

    public static synchronized void setAfk(final @NotNull Player player, final boolean value) {
        // fetch the state of the player
        var state = STATES.get(player);
        // do nothing if player is somehow not in the system
        if (state == null) {
            return;
        }
        // only do something if changing state
        var doAnnouncement = state.isAfk() != value;
        // remove any existing node from the queue
        var oldNode = state.timeoutNode();
        if (oldNode != null) {
            queue.remove(oldNode);
        }
        // create new node if necessary
        var newNode = value ? null : queue.push(new AfkNode(player));
        // create a new state
        var newState = new AfkState(player, newNode);
        STATES.put(player, newState);
        if (doAnnouncement) {
            // if we changed state, make an announcement
            var announcement = value ? "chat.afk.on" : "chat.afk.off";
            Bukkit.broadcast(Messages.get(announcement, player.displayName()));
            Bukkit.getPluginManager().callEvent(new AfkChangeEvent(player));
        }
    }

    public static synchronized void toggleAfk(final @NotNull Player player) {
        var state = STATES.get(player);
        if (state == null) {
            return;
        }
        setAfk(player, !state.isAfk());
    }

    public static synchronized void addPlayer(final @NotNull Player player) {
        if (STATES.containsKey(player)) {
            // we must not add a player that is already added
            // this should never happen but best to be safe
            return;
        }
        // proceed with adding
        var node = queue.push(new AfkNode(player));
        var state = new AfkState(player, node);
        STATES.put(player, state);
    }

    public static synchronized void removePlayer(final @NotNull Player player) {
        var state = STATES.remove(player);
        if (state != null) {
            var node = state.timeoutNode();
            if (node != null) {
                queue.remove(node);
            }
        }
    }

    public static synchronized void playerMoved(final @NotNull Player player, final @NotNull Location location) {
        var state = STATES.get(player);
        if (state != null) {
            var oldLoc = state.lastLocation();
            if (!oldLoc.getWorld().equals(location.getWorld())
                || oldLoc.distanceSquared(location) >= MIN_DIST_SQUARED) {
                // player moved far enough, they are no longer AFK
                setAfk(player, false);
            }
        }
    }

    /**
     * Check for players to automatically set as AFK.
     */
    public static synchronized void autoAfk() {
        var now = System.currentTimeMillis();
        while (true) {
            var node = queue.peek();
            if (node != null) {
                var value = node.getValue();
                if (value.time() + afkMs < now) {
                    setAfk(value.player(), true);
                    continue;
                }
            }
            return;
        }
    }
}
