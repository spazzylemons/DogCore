package net.dumbdogdiner.dogcore.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single-ended queue backed by a doubly-linked list of nodes.
 * All operations are O(1).
 * @param <E> The type of data to store in the queue.
 */
public final class LinkedQueue<E> {
    /**
     * An implementation of a node.
     * @param <E> The type of data stored in this node.
     */
    public static final class Node<E> {
        /** The value contained in the node. */
        private final E value;
        /** The previous node. */
        private @Nullable Node<E> prev;
        /** The next node. */
        private @Nullable Node<E> next = null;

        /** Prevents accidentally removing a node twice. */
        private boolean inQueue = true;

        private Node(final E nodeValue, @Nullable final Node<E> prevNode) {
            value = nodeValue;
            prev = prevNode;
        }

        @Override
        public int hashCode() {
            if (value != null) {
                return value.hashCode();
            }
            return 0;
        }

        /**
         * Nodes are only equal by identity.
         * @param other The other object to test equality with.
         * @return True if equal.
         */
        @Override
        public boolean equals(final Object other) {
            return this == other;
        }

        /**
         * Get the value stored in this node.
         * @return The value in this node.
         */
        public E getValue() {
            return value;
        }
    }

    /** The head of the list. */
    private @Nullable Node<E> head = null;

    /** The tail of the list. */
    private @Nullable Node<E> tail = null;

    /**
     * Push a new node to the back of the queue.
     * @param value The value to push.
     * @return The node at the back of the queue which contains the value.
     */
    public synchronized Node<E> push(final E value) {
        var newNode = new Node<>(value, tail);

        if (head == null) {
            head = newNode;
        }

        if (tail != null) {
            tail.next = newNode;
        }
        tail = newNode;
        return newNode;
    }

    /**
     * Get the node at the front of the queue.
     * @return The node at the front of the queue, or null.
     */
    public synchronized @Nullable Node<E> peek() {
        return head;
    }

    /**
     * Remove a node from the queue. Element order is maintained.
     * @param node The node to remove.
     */
    public synchronized void remove(@NotNull final Node<E> node) {
        if (!node.inQueue) {
            // node isn't a part of the queue, so don't do anything
            return;
        }

        var prev = node.prev;
        var next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }

        node.prev = null;
        node.next = null;
        node.inQueue = false;
    }
}
