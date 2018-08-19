package com.horizon.task

import com.horizon.task.executor.TaskExecutor

abstract class LaneTask<Params, Progress, Result> : UITask<Params, Progress, Result>() {
    override val executor: TaskExecutor
        get() = TaskCenter.lane
}
