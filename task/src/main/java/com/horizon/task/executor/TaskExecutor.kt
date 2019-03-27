package com.horizon.task.executor

import com.horizon.task.base.Priority
import java.util.concurrent.Executor

interface TaskExecutor : Executor{
     fun execute(tag: String, r: Runnable, priority: Int = Priority.NORMAL)
     fun scheduleNext(tag: String)
     fun remove(r: Runnable, priority: Int)
     fun changePriority(r: Runnable, priority: Int, increment: Int): Int

     fun execute(tag: String, r: () -> Unit, priority: Int = Priority.NORMAL) {
          execute(tag, Runnable { r.invoke() }, priority)
     }
}