package com.horizon.task

import com.horizon.task.executor.TaskExecutor

/**
 * compute-intensive task
 */

abstract class CPTask<Params, Progress, Result> : UITask<Params, Progress, Result>() {
    override val executor: TaskExecutor
        get() = TaskCenter.laneCP
}
