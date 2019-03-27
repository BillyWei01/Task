package com.horizon.task

import com.horizon.task.executor.LaneExecutor
import com.horizon.task.executor.PipeExecutor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


object TaskCenter {
    private val cpuCount = Runtime.getRuntime().availableProcessors()
    private val threadFactory = object : ThreadFactory {
        private val mCount = AtomicInteger(1)
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "Task #" + mCount.getAndIncrement())
        }
    }

    internal val poolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
            2, 256,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory)

    val io = PipeExecutor(16, 512)
    val computation = PipeExecutor(Math.min(Math.max(2, cpuCount), 4), 256)

    // 带去重策略的 Executor，可用于数据刷新等任务
    val laneIO = LaneExecutor(io, true)
    val laneCP = LaneExecutor(computation, true)

    // 相同的tag的任务会被串行执行，相当于串行的Executor
    // 可用于写日志，上报app信息等任务
    val serial = LaneExecutor(PipeExecutor(Math.min(Math.max(2, cpuCount), 4), 1024))
}