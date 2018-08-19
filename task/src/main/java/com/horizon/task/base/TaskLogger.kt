package com.horizon.task.base


interface TaskLogger {
    val isDebug: Boolean

    fun e(tag: String, e: Throwable)
}
