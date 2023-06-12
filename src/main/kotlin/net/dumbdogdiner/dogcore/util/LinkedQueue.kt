package net.dumbdogdiner.dogcore.util

/**
 * A single-ended queue backed by a doubly-linked list of nodes. All operations are O(1).
 */
class LinkedQueue<E> {
    /** The public details of nodes. */
    sealed interface Node<E> {
        /** The value contained in the node. */
        val value: E
    }

    /** An implementation of a node. */
    private class NodeImpl<E>(
        /** The value contained in the node. */
        override val value: E,
        /** The previous node. */
        var prev: NodeImpl<E>?,
        /** The next node. */
        var next: NodeImpl<E>?,
        /** Prevents accidentally removing a node twice. */
        var inQueue: Boolean = true
    ) : Node<E> {
        override fun hashCode() = value.hashCode()

        /** Nodes are only equal by identity. */
        override fun equals(other: Any?) = this === other
    }

    /** The head of the list. */
    private var head: NodeImpl<E>? = null

    /** The tail of the list. */
    private var tail: NodeImpl<E>? = null

    /** Push a new node to the back of the queue. */
    fun push(value: E): Node<E> {
        val newNode = NodeImpl(value, tail, null)

        if (head == null) {
            head = newNode
        }

        tail?.let {
            it.next = newNode
        }

        tail = newNode
        return newNode
    }

    /** Get the node at the front of the queue. */
    fun peek(): Node<E>? {
        return head
    }

    /** Remove a node from the queue. Element order is maintained. */
    fun remove(node: Node<E>) {
        val nodeImpl = node as NodeImpl<E>
        if (!nodeImpl.inQueue) {
            // node isn't a part of the queue, so don't do anything
            return
        }

        nodeImpl.prev?.let {
            it.next = nodeImpl.next
        } ?: run {
            head = nodeImpl.next
        }

        nodeImpl.next?.let {
            it.prev = nodeImpl.prev
        } ?: run {
            tail = nodeImpl.prev
        }

        nodeImpl.prev = null
        nodeImpl.next = null
        nodeImpl.inQueue = false
    }
}
