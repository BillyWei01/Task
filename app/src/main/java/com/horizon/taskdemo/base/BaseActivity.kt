package com.horizon.taskdemo.base

import android.app.Activity
import android.content.Intent

import com.horizon.task.lifecycle.Event
import com.horizon.task.lifecycle.LifecycleManager

abstract class BaseActivity : Activity() {
    protected val tag = this.javaClass.simpleName!!

    override fun onDestroy() {
        super.onDestroy()
        LifecycleManager.notify(this, Event.DESTROY)
    }

    override fun onPause() {
        super.onPause()
        LifecycleManager.notify(this, Event.HIDE)
    }

    override fun onResume() {
        super.onResume()
        LifecycleManager.notify(this, Event.SHOW)
    }

    fun startActivity(activityClazz: Class<*>) {
        val intent = Intent(this, activityClazz)
        startActivity(intent)
    }
}
