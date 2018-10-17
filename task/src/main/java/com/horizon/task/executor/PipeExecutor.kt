package com.horizon.task.executor

import com.horizon.task.TaskCenter
import com.horizon.task.base.Priority
import com.horizon.task.base.PriorityQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

/**
 * Executor to schedule tasks
 *
 * Tasks can assign [Priority], tasks in some priority execute in FIFO order.
 *
 * [windowSize] control the Executor's concurrency,
 * if [windowSize] = 1, the Executor become a serial poolExecutor.
 */
open class PipeExecutor @JvmOverloads constructor(
        windowSize: Int,
        private val capacity: Int = -1,
        private val rejectedHandler: RejectedExecutionHandler = defaultHandler) : TaskExecutor{

    private val tasks = PriorityQueue<PriorityRunnable>()
    private val windowSize: Int = if (windowSize > 0) windowSize else 1
    private var count = 0

    companion object {
        val defaultHandler = ThreadPoolExecutor.AbortPolicy()
    }

    @Synchronized
    override fun execute(r: Runnable, tag: String, priority: Int, finish: (tag: String) -> Unit) {
        if(capacity > 0 && count + tasks.size() >= capacity){
            rejectedHandler.rejectedExecution(r, TaskCenter.poolExecutor)
        }
        val active = PriorityRunnable(r, tag, finish)
        if (count < windowSize || priority == Priority.IMMEDIATE) {
            startTask(active)
        } else {
            tasks.offer(active, priority)
        }
    }

    override fun execute(r: Runnable) {
        execute(r, "")
    }

    @Synchronized
    override fun remove(r: Runnable, priority: Int) {
        tasks.remove(r, priority)
    }

    @Synchronized
    override fun changePriority(r: Runnable, priority: Int, increment: Int): Int {
        val active = tasks.remove(r, priority)
        if (active != null) {
            val newPriority = priority + increment
            tasks.offer(active, newPriority)
            return newPriority
        }
        return priority
    }

    @Synchronized
    override fun scheduleNext(tag: String) {
        count--
        if (count < windowSize) {
            startTask(tasks.poll())
        }
    }

    private fun startTask(active: Runnable?) {
        if (active != null) {
            count++
            TaskCenter.poolExecutor.execute(active)
        }
    }

    @Suppress("EqualsOrHashCode")
    private inner class PriorityRunnable internal constructor(
            private val r: Runnable,
            private val tag: String,
            private val finish: (tag: String) -> Unit) : Runnable {
        override fun run() {
            try {
                r.run()
            } finally {
                scheduleNext(tag)
                if(!tag.isEmpty()){
                    finish(tag)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            return this === other || r === other
        }
    }
}
