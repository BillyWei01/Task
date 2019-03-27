package com.horizon.task.executor

import com.horizon.task.base.CircularQueue
import java.util.*
import java.util.concurrent.Future

/**
 * [LaneExecutor] 主要用于避免任务重复执行。
 *
 * 其原理为：
 * 1、给任务打tag，相同tag的任务视为相同的任务；
 * 2、记录进入调度的任务（委托给[executor]);
 * 3、任务提交过来，如果已经有相同的任务在进入调度，需要等待；
 * 4、若需要等待,有两种模式：限制模式和非限制模式，
 *    限制模式，只能有一个任务等待执行，过滤后面的任务;
 *    非限制模式，都放入FIFO的队列（[waitingQueues]）中。
 *
 * 效果：
 * 1、tag相同的任务会串行，不同tag的任务会并发执行;
 * 2、限制模式可以过滤冗余执行，
 *    比如“收到通知->重新加载->显示”的任务，即使（几乎）同时收到通知，
 *    最多也只是一个执行，一个等待（防止前面的任务加载完了但是任务没结束，从而不能正确更新);
 * 3、非限制模式可以防止重复执行,
 *    比如图片加载，当相同的源的任务同时发起，如果并行执行，则会重复加载，
 *    如果使之串行，配合缓存，第一个任务完成后，后面的任务可以直接取缓存，而避免重复加载；
 *    此外，非限制模式下，还可以当串行执行器用（tag相同串行执行，任务队列无限容量）。
 */
class LaneExecutor(private val executor: PipeExecutor, private val limit: Boolean = false) : TaskExecutor {
    // 正在调度的任务
    private val scheduledTasks = HashMap<String, Runnable>()
    // 限制模式下等待的任务
    private val waitingTasks by lazy { HashMap<String, TaskWrapper>() }
    // 非限制模式下等待的任务
    private val waitingQueues by lazy { HashMap<String, CircularQueue<TaskWrapper>>() }

    private class TaskWrapper(val r: Runnable, val priority: Int)

    private val finishCallback: (tag: String) -> Unit = { tag ->
        scheduleNext(tag)
    }

    /**
     * 常规执行，不会串行
     */
    override fun execute(r: Runnable) {
        executor.execute(r)
    }

    @Synchronized
    override fun execute(tag: String, r: Runnable, priority: Int) {
        if (tag.isEmpty()) {
            executor.schedule(tag, r, priority)
        } else if (!scheduledTasks.containsKey(tag)) {
            start(r, tag, priority)
        } else if (limit) {
            if (waitingTasks.containsKey(tag)) {
                if (r is Future<*>) {
                    r.cancel(false)
                }
            } else {
                waitingTasks[tag] = TaskWrapper(r, priority)
            }
        } else {
            val queue = waitingQueues[tag]
                    ?: CircularQueue<TaskWrapper>().apply { waitingQueues[tag] = this }
            queue.offer(TaskWrapper(r, priority))
        }
    }

    private fun start(r: Runnable, tag: String, priority: Int) {
        scheduledTasks[tag] = r
        executor.schedule(tag, r, priority, finishCallback)
    }

    @Synchronized
    override fun scheduleNext(tag: String) {
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

    override fun remove(r: Runnable, priority: Int) {
        executor.remove(r, priority)
    }

    override fun changePriority(r: Runnable, priority: Int, increment: Int): Int {
        return executor.changePriority(r, priority, increment)
    }
}