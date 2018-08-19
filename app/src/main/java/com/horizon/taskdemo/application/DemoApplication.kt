package com.horizon.taskdemo.application

import android.app.Application
import android.util.Log

import com.horizon.task.base.LogProxy
import com.horizon.task.base.TaskLogger
import com.horizon.taskdemo.BuildConfig
import com.horizon.taskdemo.base.HttpClient


class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        HttpClient.init(this)

        LogProxy.init(object : TaskLogger {
            override val isDebug: Boolean
                get() = BuildConfig.DEBUG

            override fun e(tag: String, e: Throwable) {
                Log.e(tag, e.message, e)
            }
        })
    }
}
