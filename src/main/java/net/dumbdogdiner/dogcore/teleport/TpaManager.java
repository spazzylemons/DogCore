package net.dumbdogdiner.dogcore.teleport;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.util.LinkedQueue;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TpaManager {
    private static final long TPA_EXPIRE_MS = 120L * 1000L;

    private record TpaConnection(boolean here, long time) {}

    /** A network of users and their TPA requests. */
    private static final MutableNetwork<UUID, LinkedQueue.Node<TpaConnection>> requestNetwork = NetworkBuilder.directed().build();

    /** The head of the TPA timeout queue. */
    private static final LinkedQueue<TpaConnection> timeoutQueue = new LinkedQueue<>();

    private static @Nullable LinkedQueue.Node<TpaConnection> getEdge(@NotNull UUID from, @NotNull UUID to) {
        try {
            return requestNetwork.edgeConnecting(from, to).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void performMaintenance() {
        var now = System.currentTimeMillis();
        while (true) {
            var edge = timeoutQueue.peek();
            if (edge == null || edge.getValue().time + TPA_EXPIRE_MS > now) {
                return;
            }
            removeEdge(edge);
        }
    }

    private static void removeIfUnused(@NotNull UUID node) {
        try {
            if (requestNetwork.degree(node) == 0) {
                requestNetwork.removeNode(node);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void removeEdge(@NotNull LinkedQueue.Node<TpaConnection> edge) {
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

    private static void addRequest(@NotNull UUID from, @NotNull UUID to, @NotNull TpaConnection connection) {
        performMaintenance();
        var edge = timeoutQueue.push(connection);
        requestNetwork.addEdge(from, to, edge);
    }

    private static @Nullable TpaConnection takeRequest(@NotNull UUID from, @NotNull UUID to) {
        performMaintenance();
        var edge = getEdge(from, to);
        if (edge != null) {
            removeEdge(edge);
            return edge.getValue();
        }
        return null;
    }

    public static void request(@NotNull Player from, @NotNull Player to, boolean here) {
        if (from == to) {
            from.sendMessage(Messages.get("commands.tpa.samePlayer"));
            return;
        }
        // remove any existing request
        takeRequest(from.getUniqueId(), to.getUniqueId());
        // add a new request
        var request = new TpaConnection(here, System.currentTimeMillis());
        addRequest(from.getUniqueId(), to.getUniqueId(), request);

        var fromName = NameFormatter.formatUsername(from).join();
        var toName = NameFormatter.formatUsername(to).join();

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

    public static void removePlayer(@NotNull UUID uuid) {
        requestNetwork.removeNode(uuid);
    }

    public static void accept(@NotNull Player from, @NotNull Player to) {
        var request = takeRequest(from.getUniqueId(), to.getUniqueId());
        if (request != null) {
            var name = NameFormatter.formatUsername(to).join();
            from.sendMessage(Messages.get("commands.tpaccept.from", name));
            to.sendMessage(Messages.get("commands.tpaccept"));

            if (request.here) {
                SafeTeleport.safeTeleport(from, to.getLocation());
            } else {
                SafeTeleport.safeTeleport(to, from.getLocation());
            }
        } else {
            from.sendMessage(Messages.get("commands.tpa.nothing"));
        }
    }

    public static void deny(@NotNull Player from, @NotNull Player to) {
        var request = takeRequest(from.getUniqueId(), to.getUniqueId());
        if (request != null) {
            var name = NameFormatter.formatUsername(to).join();
            from.sendMessage(Messages.get("commands.tpdeny.from", name));
            to.sendMessage(Messages.get("commands.tpdeny"));
        } else {
            from.sendMessage(Messages.get("commands.tpa.nothing"));
        }
    }
}
