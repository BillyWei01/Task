package com.horizon.taskdemo.base

import com.horizon.task.TaskCenter
import com.horizon.task.executor.PipeExecutor
import rx.Scheduler
import rx.schedulers.Schedulers

object TaskSchedulers {
    val io: Scheduler by lazy { Schedulers.from(TaskCenter.io) }
    val computation: Scheduler by lazy { Schedulers.from(TaskCenter.computation) }
    val single by lazy { Schedulers.from(PipeExecutor(1)) }
}
