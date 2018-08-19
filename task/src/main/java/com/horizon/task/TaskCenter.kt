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
            0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(),
            threadFactory)

    // standard Executor
    val io = PipeExecutor(16, 512)
    val computation = PipeExecutor(Math.min(Math.max(2, cpuCount), 6), 256)

    // use to execute tasks which need to run in serial,
    // such as writing logs, reporting app info to server ...
    val lane = LaneExecutor(PipeExecutor(Math.min(Math.max(2, cpuCount), 4), 512))

    // use to execute general tasks，such as loading data.
    val laneIO = LaneExecutor(io, true)
    val laneCP = LaneExecutor(computation, true)
}