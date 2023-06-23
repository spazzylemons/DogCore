package net.dumbdogdiner.dogcore.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import net.dumbdogdiner.dogcore.config.Configurable;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.util.LinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class AfkManager implements Listener, Runnable, Configurable {
    private AfkManager() { }

    /** The time it takes to turn AFK. */
    private static long afkMs;

    /** The distance squared needed to move to not be marked AFK. */
    public static final double MIN_DIST_SQUARED = 16.0;

    /** Ticks in a second. */
    private static final int TPS = 20;

    /** The AFK command itself. Used to avoid clearing AFK as a side effect of running /afk. */
    private static final String AFK_COMMAND = "/afk";

    /** The AFK states for each player. */
    private static final Map<@NotNull Player, @NotNull AfkState> STATES = new HashMap<>();

    /** The queue of non-AFK players. */
    private static LinkedQueue<@NotNull AfkNode> queue;

    public static void init(final @NotNull Plugin plugin) {
        // create an instance for events
        var instance = new AfkManager();
        // establish a repeating task to query the player list
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, instance, 0, TPS);
        // register listeners as well
        Bukkit.getPluginManager().registerEvents(instance, plugin);
        // register configurable
        Configuration.register(instance);
    }

    private static synchronized void setAfk(final @NotNull Player player, final boolean value) {
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
        }
    }

    public static synchronized void toggleAfk(final @NotNull Player player) {
        var state = STATES.get(player);
        if (state == null) {
            return;
        }
        setAfk(player, !state.isAfk());
    }

    @EventHandler
    public void onJoin(final @NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        // add the player to the system
        synchronized (AfkManager.class) {
            var node = queue.push(new AfkNode(player));
            var state = new AfkState(player, node);
            STATES.put(player, state);
        }
    }

    @EventHandler
    public void onQuit(final @NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        // remove the player from the system
        synchronized (AfkManager.class) {
            var state = STATES.remove(player);
            if (state != null) {
                var node = state.timeoutNode();
                if (node != null) {
                    queue.remove(node);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(final @NotNull PlayerMoveEvent event) {
        var player = event.getPlayer();
        synchronized (AfkManager.class) {
            var state = STATES.get(player);
            if (state != null) {
                var oldLoc = state.lastLocation();
                var newLoc = event.getTo();
                if (!oldLoc.getWorld().equals(newLoc.getWorld())
                    || oldLoc.distanceSquared(newLoc) >= MIN_DIST_SQUARED) {
                    // player moved far enough, they are no longer AFK
                    setAfk(player, false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(final @NotNull AsyncChatEvent event) {
        setAfk(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(final @NotNull PlayerCommandPreprocessEvent event) {
        if (!AFK_COMMAND.equals(event.getMessage())) {
            setAfk(event.getPlayer(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            setAfk(event.getPlayer(), false);
        }
    }

    /**
     * Check for players to automatically set as AFK.
     */
    @Override
    public void run() {
        var now = System.currentTimeMillis();
        synchronized (AfkManager.class) {
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

    @Override
    public void loadConfig() {
        var time = Duration.ofSeconds(Configuration.getInt("afk.timeout")).toMillis();
        synchronized (AfkManager.class) {
            queue = new LinkedQueue<>();
            for (var entry : STATES.entrySet()) {
                var value = entry.getValue();
                var node = queue.push(new AfkNode(entry.getKey()));
                entry.setValue(new AfkState(value.lastLocation(), node));
            }
            afkMs = time;
        }
    }
}
