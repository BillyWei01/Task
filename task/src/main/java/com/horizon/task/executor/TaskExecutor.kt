package com.horizon.task.executor

import com.horizon.task.base.Priority
import java.util.concurrent.Executor

interface TaskExecutor : Executor{
     fun remove(r: Runnable, priority: Int)
     fun execute(r: Runnable, tag: String, priority: Int = Priority.NORMAL,
                finish: (tag: String) -> Unit = {})
     fun changePriority(r: Runnable, priority: Int, increment: Int): Int
}