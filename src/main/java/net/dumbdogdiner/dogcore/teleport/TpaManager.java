package net.dumbdogdiner.dogcore.teleport;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.time.Duration;
import java.util.Set;
import net.dumbdogdiner.dogcore.config.Configurable;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.util.LinkedQueue;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TpaManager implements Listener, Runnable, Configurable {
    private TpaManager() { }

    /** The time in which TPA expires. */
    private static long tpaExpireMs;

    /** The time to repeat the maintenance task (Every minute). */
    private static final long MAINTENANCE_TICKS = 20L * 60L;

    private record TpaConnection(boolean here, long time) { }

    /** A network of users and their TPA requests. */
    private static MutableNetwork<UUID, LinkedQueue.Node<TpaConnection>> requestNetwork;

    /** The head of the TPA timeout queue. */
    private static LinkedQueue<TpaConnection> timeoutQueue;

    public static void init(final @NotNull Plugin plugin) {
        var instance = new TpaManager();
        // register the repeating maintenance task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, instance, 0L, MAINTENANCE_TICKS);
        // register event to remove players that leave
        Bukkit.getPluginManager().registerEvents(instance, plugin);
        // register configurable
        Configuration.register(instance);
    }

    private static synchronized LinkedQueue.@Nullable Node<TpaConnection> getEdge(
        @NotNull final UUID from,
        @NotNull final UUID to
    ) {
        try {
            return requestNetwork.edgeConnecting(from, to).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static synchronized void performMaintenance() {
        var now = System.currentTimeMillis();
        while (true) {
            var edge = timeoutQueue.peek();
            if (edge == null || edge.getValue().time + tpaExpireMs > now) {
                return;
            }
            removeEdge(edge);
        }
    }

    private static synchronized void removeIfUnused(@NotNull final UUID node) {
        try {
            if (requestNetwork.degree(node) == 0) {
                requestNetwork.removeNode(node);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static synchronized void removeEdge(@NotNull final LinkedQueue.Node<TpaConnection> edge) {
        // remove from the queue
        timeoutQueue.remove(edge);
        // check what nodes were connected to this edge
        EndpointPair<UUID> pair;
        try {
            pair = requestNetwork.incidentNodes(edge);
        } catch (IllegalArgumentException e) {
            pair = null;
        }
        // remove the edge
        requestNetwork.removeEdge(edge);
        // remove nodes if unused
        if (pair != null) {
            removeIfUnused(pair.nodeU());
            removeIfUnused(pair.nodeV());
        }
    }

    private static synchronized void addRequest(
        @NotNull final UUID from,
        @NotNull final UUID to,
        @NotNull final TpaConnection connection
    ) {
        performMaintenance();
        var edge = timeoutQueue.push(connection);
        requestNetwork.addEdge(from, to, edge);
    }

    private static synchronized @Nullable TpaConnection takeRequest(
        @NotNull final UUID from,
        @NotNull final UUID to
    ) {
        performMaintenance();
        var edge = getEdge(from, to);
        if (edge != null) {
            removeEdge(edge);
            return edge.getValue();
        }
        return null;
    }

    /**
     * Create a TPA request.
     * @param from The user making the request.
     * @param to The user to send the request to.
     * @param here If true, the direction of the teleport is reversed.
     */
    public static synchronized void request(
        @NotNull final Player from,
        @NotNull final Player to,
        final boolean here
    ) {
        if (from == to) {
            from.sendMessage(Messages.get("commands.tpa.samePlayer"));
            return;
        }
        // remove any existing request
        takeRequest(from.getUniqueId(), to.getUniqueId());
        // add a new request
        var request = new TpaConnection(here, System.currentTimeMillis());
        addRequest(from.getUniqueId(), to.getUniqueId(), request);

        var fromName = from.displayName();
        var toName = to.displayName();

        from.sendMessage(Messages.get("commands.tpa.sent", toName));
        var accept = Messages.get("commands.tpa.accept")
            .clickEvent(ClickEvent.runCommand("/tpaccept ${from.name}"));
        var deny = Messages.get("commands.tpa.deny")
            .clickEvent(ClickEvent.runCommand("/tpdeny ${from.name}"));
        if (here) {
            to.sendMessage(Messages.get("commands.tpahere.received", fromName, accept, deny));
        } else {
            to.sendMessage(Messages.get("commands.tpa.received", fromName, accept, deny));
        }
    }

    /**
     * Remove a player from the TPA manager.
     * @param uuid User to remove.
     */
    private static synchronized void removePlayer(@NotNull final UUID uuid) {
        Set<LinkedQueue.Node<TpaConnection>> edges;
        try {
            edges = requestNetwork.incidentEdges(uuid);
        } catch (IllegalArgumentException e) {
            // nothing to remove
            return;
        }
        // remove all edges - this will clean up the node once no more incident edges remain
        for (var edge : edges) {
            removeEdge(edge);
        }
    }

    /**
     * Accept a TPA request.
     * @param from User who requested TPA.
     * @param to User who received the request.
     */
    public static synchronized void accept(@NotNull final Player from, @NotNull final Player to) {
        var request = takeRequest(from.getUniqueId(), to.getUniqueId());
        if (request != null) {
            var name = to.displayName();
            from.sendMessage(Messages.get("commands.tpaccept.from", name));
            to.sendMessage(Messages.get("commands.tpaccept"));

            if (request.here) {
                TeleportHelper.safeTeleport(from, to.getLocation());
            } else {
                TeleportHelper.safeTeleport(to, from.getLocation());
            }
        } else {
            from.sendMessage(Messages.get("commands.tpa.nothing"));
        }
    }

    /**
     * Deny a TPA request.
     * @param from User who requested TPA.
     * @param to User who received the request.
     */
    public static synchronized void deny(@NotNull final Player from, @NotNull final Player to) {
        var request = takeRequest(from.getUniqueId(), to.getUniqueId());
        if (request != null) {
            var name = to.displayName();
            from.sendMessage(Messages.get("commands.tpdeny.from", name));
            to.sendMessage(Messages.get("commands.tpdeny"));
        } else {
            from.sendMessage(Messages.get("commands.tpa.nothing"));
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        removePlayer(event.getPlayer().getUniqueId());
    }

    @Override
    public void run() {
        // When the TPA manager is ticked, we will perform maintenance on the network and queue.
        performMaintenance();
    }

    @Override
    public void loadConfig() {
        var expireMs = Duration.ofSeconds(Configuration.getInt("tpa.timeout")).toMillis();
        synchronized (TpaManager.class) {
            tpaExpireMs = expireMs;
            requestNetwork = NetworkBuilder.directed().build();
            timeoutQueue = new LinkedQueue<>();
        }
    }
}
