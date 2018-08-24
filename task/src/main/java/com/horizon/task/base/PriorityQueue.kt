package com.horizon.task.base

import com.horizon.task.UITask
import com.horizon.task.executor.PipeExecutor

/**
 * Custom PriorityQueue for [PipeExecutor.tasks]
 *
 * We have tried to use [java.util.PriorityQueue],
 * but failed in [java.util.PriorityQueue.remove] .
 *
 * Elements in queue my be [PipeExecutor.PriorityRunnable], which is a wrapper of runnable,
 * and we need to find element by runnable,
 * it return false when compare object with element like 'o.equals(queue[0])'.
 * If compare like 'queue[0].equals(o)', it will return true, for we have override equals(other).

 * So we make this custom PriorityQueue.
 *
 * Tasks offer to queue may be [Priority.HIGH] , [Priority.NORMAL], [Priority.LOW],
 * and when Activity/Fragment change visibility from show to hidden, priority decrease,
 * so we just need four [CircularQueue] to manager the tasks.
 *
 * @see PipeExecutor
 * @see Priority
 * @see UITask.priority
 * @see UITask.onEvent
 */
internal class PriorityQueue<E> {
    private var size = 0
    private val queues: Array<CircularQueue<E>> =
         arrayOf(CircularQueue(), CircularQueue(), CircularQueue(), CircularQueue())

    fun offer(data: E, priority: Int) {
        val p = (priority + 2) and 3
        queues[p].offer(data)
        size++
    }

    fun poll(): E? {
        if (size == 0) {
            return null
        }
        size--
        for (i in 3 downTo 0) {
            val e = queues[i].poll()
            if (e != null) {
                return e
            }
        }
        return null
    }

    fun remove(o: Any, priority: Int): E? {
        val p = (priority + 2) and 3
        val target = queues[p].remove(o)
        if (target != null) {
            size--
        }
        return target
    }

    fun size(): Int {
        return size
    }
}
