package com.horizon.task.executor

import android.util.Log
import com.horizon.task.base.CircularQueue
import com.horizon.task.base.LogProxy
import java.util.*
import java.util.concurrent.Future

/**
 * Identify tasks with tag
 *
 * Tasks with same tag process serial;
 * Tasks with different tag process concurrent.
 *
 * The tasks with same tag, only one task can put into [executor],
 * if [limit] is true, only one task can wait to be scheduled, tasks coming later will be ignored.
 * otherwise, all tasks coming later will be put into [waitingQueues]
 *
 * It's properly to use to execute tasks such as refreshing data,
 * which need to avoid reloading data repeatedly or miss updating.
 */
class LaneExecutor(private val executor: PipeExecutor, private val limit: Boolean = false) : TaskExecutor {
    private val scheduledTasks = HashMap<String, Runnable>()
    private val waitingQueues by lazy { HashMap<String, CircularQueue<TaskWrapper>>() }
    private val waitingTasks by lazy { HashMap<String, TaskWrapper>() }

    private class TaskWrapper(val r: Runnable, val priority: Int)

    private val finishCallback: (tag: String) -> Unit = { tag ->
        synchronized(LaneExecutor@ this) {
            scheduledTasks.remove(tag)
            if (limit) {
                waitingTasks.remove(tag)?.let { start(it.r, tag, it.priority) }
            } else {
                waitingQueues[tag]?.let {
                    val wrapper = it.poll()
                    if (wrapper == null) {
                        waitingQueues.remove(tag)
                    } else {
                        start(wrapper.r, tag, wrapper.priority)
                    }
                }
            }
        }
    }

    @Synchronized
    override fun execute(r: Runnable, tag: String, priority: Int, finish: (tag: String) -> Unit) {
        if (scheduledTasks.containsKey(tag)) {
            if (limit) {
                if (waitingTasks.containsKey(tag)) {
                    if (r is Future<*>) {
                        r.cancel(false)
                    }
                    if (LogProxy.isDebug) {
                        Log.d(tag, "ignore")
                    }
                } else {
                    waitingTasks[tag] = TaskWrapper(r, priority)
                    if (LogProxy.isDebug) {
                        Log.d(tag, "waiting")
                    }
                }
            } else {
                val queue = waitingQueues[tag]
                        ?: CircularQueue<TaskWrapper>().apply { waitingQueues[tag] = this }
                queue.offer(TaskWrapper(r, priority))
                if (LogProxy.isDebug) {
                    Log.d(tag, "waiting")
                }
            }
        } else {
            start(r, tag, priority)
        }
    }

    private fun start(r: Runnable, tag: String, priority: Int) {
        scheduledTasks[tag] = r
        executor.execute(r, tag, priority, finishCallback)
    }

    override fun execute(r: Runnable) {
        executor.execute(r)
    }

    override fun remove(r: Runnable, priority: Int) {
        executor.remove(r, priority)
    }

    override fun changePriority(r: Runnable, priority: Int, increment: Int): Int {
        return executor.changePriority(r, priority, increment)
    }
}