package com.horizon.task.base


object LogProxy : TaskLogger {
    private var subject: TaskLogger? = null

    override val isDebug: Boolean
        get() = subject?.isDebug ?: false

    /**
     * init LogProxy when app start
     */
    fun init(realSubject: TaskLogger) {
        subject = realSubject
    }

    override fun e(tag: String, e: Throwable) {
        subject?.e(tag, e)
    }
}
