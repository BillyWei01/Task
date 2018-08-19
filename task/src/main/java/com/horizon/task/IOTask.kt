package com.horizon.task

import com.horizon.task.executor.TaskExecutor

/**
 * io-intensive task
 */
abstract class IOTask<Params, Progress, Result> : UITask<Params, Progress, Result>() {
    override val executor: TaskExecutor
        get() = TaskCenter.laneIO
}
