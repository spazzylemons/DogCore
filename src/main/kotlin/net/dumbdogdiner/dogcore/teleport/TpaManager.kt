package net.dumbdogdiner.dogcore.teleport

import com.google.common.graph.NetworkBuilder
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.messages.Messages
import net.dumbdogdiner.dogcore.util.LinkedQueue
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

private data class TpaConnection(val here: Boolean, val time: Long)

private typealias Edge = LinkedQueue.Node<TpaConnection>

@Suppress("UnstableApiUsage")
object TpaManager {
    private const val TPA_EXPIRE_MS = 120L * 1000L

    /** A network of users and their TPA requests. */
    private val requestNetwork = NetworkBuilder.directed()
        .build<UUID, Edge>()

    /** The head of the TPA timeout queue. */
    private val timeoutQueue = LinkedQueue<TpaConnection>()

    private fun getEdge(from: UUID, to: UUID) = try {
        requestNetwork.edgeConnecting(from, to).getOrNull()
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun performMaintenance() {
        val now = System.currentTimeMillis()
        while (true) {
            val edge = timeoutQueue.peek() ?: return
            if (edge.value.time + TPA_EXPIRE_MS <= now) {
                removeEdge(edge)
            } else {
                return
            }
        }
    }

    private fun removeIfUnused(node: UUID) {
        try {
            if (requestNetwork.degree(node) == 0) {
                requestNetwork.removeNode(node)
            }
        } catch (e: IllegalArgumentException) {
            return
        }
    }

    private fun removeEdge(edge: Edge) {
        // remove from the queue
        timeoutQueue.remove(edge)
        // check what nodes were connected to this edge
        val pair = try {
            requestNetwork.incidentNodes(edge)
        } catch (e: IllegalArgumentException) {
            null
        }
        // remove the edge
        requestNetwork.removeEdge(edge)
        // remove nodes if unused
        if (pair != null) {
            removeIfUnused(pair.nodeU())
            removeIfUnused(pair.nodeV())
        }
    }

    private fun addRequest(from: UUID, to: UUID, connection: TpaConnection) {
        performMaintenance()
        val edge = timeoutQueue.push(connection)
        requestNetwork.addEdge(from, to, edge)
    }

    private fun takeRequest(from: UUID, to: UUID): TpaConnection? {
        performMaintenance()
        val edge = getEdge(from, to)
        if (edge != null) {
            removeEdge(edge)
            return edge.value
        }
        return null
    }

    suspend fun request(from: Player, to: Player, here: Boolean) {
        if (from == to) {
            from.sendMessage(Messages["commands.tpa.samePlayer"])
            return
        }
        // remove any existing request
        takeRequest(from.uniqueId, to.uniqueId)
        // add a new request
        val request = TpaConnection(here, System.currentTimeMillis())
        addRequest(from.uniqueId, to.uniqueId, request)

        val fromName = NameFormatter.formatUsername(from)
        val toName = NameFormatter.formatUsername(to)

        from.sendMessage(Messages["commands.tpa.sent", toName])
        val accept = Messages["commands.tpa.accept"]
            .clickEvent(ClickEvent.runCommand("/tpaccept ${from.name}"))
        val deny = Messages["commands.tpa.deny"]
            .clickEvent(ClickEvent.runCommand("/tpdeny ${from.name}"))
        if (here) {
            to.sendMessage(Messages["commands.tpahere.received", fromName, accept, deny])
        } else {
            to.sendMessage(Messages["commands.tpa.received", fromName, accept, deny])
        }
    }

    fun removePlayer(uuid: UUID) {
        requestNetwork.removeNode(uuid)
    }

    suspend fun accept(from: Player, to: Player) {
        val request = takeRequest(from.uniqueId, to.uniqueId)
        if (request != null) {
            val name = NameFormatter.formatUsername(to)
            from.sendMessage(Messages["commands.tpaccept.from", name])
            to.sendMessage(Messages["commands.tpaccept"])

            if (request.here) {
                safeTeleport(from, to.location)
            } else {
                safeTeleport(to, from.location)
            }
        } else {
            from.sendMessage(Messages["commands.tpa.nothing"])
        }
    }

    suspend fun deny(from: Player, to: Player) {
        val request = takeRequest(from.uniqueId, to.uniqueId)
        if (request != null) {
            val name = NameFormatter.formatUsername(to)
            from.sendMessage(Messages["commands.tpdeny.from", name])
            to.sendMessage(Messages["commands.tpdeny"])
        } else {
            from.sendMessage(Messages["commands.tpa.nothing"])
        }
    }
}
