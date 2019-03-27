package com.horizon.task.executor

import com.horizon.task.TaskCenter
import com.horizon.task.base.Priority
import com.horizon.task.base.PriorityQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

/**
 * [PipeExecutor] 主要用于控制任务并发
 *
 * PipeExecutor 支持任务优先级，相同优先级的任务按先进先出的顺序执行。
 *
 * [windowSize]: 并发窗口，控制同时执行的任务数量；
 * 若 [windowSize] = 1, 相当于串行执行器。
 *
 * [capacity]: 任务容量限制(包括正在执行的任务和缓冲的任务），超过容量会执行[rejectedHandler]，
 * 若 [capacity] <= 0, 不限制容量。
 */
class PipeExecutor @JvmOverloads constructor(
        windowSize: Int,
        private val capacity: Int = -1,
        private val rejectedHandler: RejectedExecutionHandler = defaultHandler) : TaskExecutor {

    private val tasks = PriorityQueue<PriorityRunnable>()
    private val windowSize: Int = if (windowSize > 0) windowSize else 1
    private var count = 0

    companion object {
        val defaultHandler = ThreadPoolExecutor.AbortPolicy()
    }

    override fun execute(r: Runnable) {
        schedule("", r, Priority.NORMAL)
    }

    fun execute(r: Runnable, priority: Int) {
        schedule("", r, priority)
    }

    // 在PipeExecutor中tag没什么用, 只是为了统一形式，方便 UITask 调用
    override fun execute(tag: String, r: Runnable, priority: Int) {
        schedule(tag, r, priority)
    }

    /**
     * @param tag
     * @param r
     * @param priority
     * @param finishCallback
     */
    @Synchronized
    internal fun schedule(tag: String, r: Runnable, priority: Int, finishCallback: (tag: String) -> Unit = {}) {
        if (capacity > 0 && (tasks.size() + count) >= capacity) {
            rejectedHandler.rejectedExecution(r, TaskCenter.poolExecutor)
        }
        val active = PriorityRunnable(r, tag, finishCallback)
        if (count < windowSize || priority == Priority.IMMEDIATE) {
            startTask(active)
        } else {
            tasks.offer(active, priority)
        }
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

    @Suppress("EqualsOrHashCode")
    private inner class PriorityRunnable internal constructor(
            private val r: Runnable,
            private val tag: String,
            private val finish: (tag: String) -> Unit) : Runnable {
        override fun run() {
            try {
                r.run()
            } finally {
                scheduleNext("")
                if (!tag.isEmpty()) {
                    finish(tag)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            return this === other || r === other
        }
    }
}
