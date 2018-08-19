package com.horizon.task.base

/**
 * FIFO queue
 *
 * We have tried to [java.util.LinkedList],
 * failed in [java.util.LinkedList.remove],
 * same reason with [PriorityQueue]
 */
internal class CircularQueue<E> {
    private var head: Node<E>? = null
    private var tail: Node<E>? = null

    private class Node<E> internal constructor(internal var data: E) {
        internal var next: Node<E>? = null
    }

    internal fun offer(data: E) {
        val next = Node(data)
        if (head == null) {
            head = next
            tail = next
        } else {
            tail!!.next = next
            tail = next
        }
    }

    internal fun poll(): E? {
        if (head == null) {
            return null
        }
        val e = head!!.data
        head = head!!.next
        if (head == null) {
            tail = null
        }
        return e
    }

    internal fun remove(o: Any): E? {
        if (head == null) {
            return null
        }
        if (head!!.data == o) {
            return poll()
        }
        val prev: Node<E> = head!!
        var curr: Node<E>? = prev.next
        while (curr != null) {
            if (curr.data == o) {
                if (tail === curr) {
                    tail = prev
                }
                prev.next = curr.next
                return curr.data
            } else {
                curr = curr.next
            }
        }
        return null
    }
}