package com.horizon.taskdemo.base

import android.app.Activity
import android.content.Intent
import com.horizon.task.lifecycle.LifeEvent

import com.horizon.task.lifecycle.LifecycleManager

abstract class BaseActivity : Activity() {
    protected val tag = this.javaClass.simpleName!!

    override fun onDestroy() {
        super.onDestroy()
        LifecycleManager.notify(this, LifeEvent.DESTROY)
    }

    override fun onPause() {
        super.onPause()
        LifecycleManager.notify(this, LifeEvent.HIDE)
    }

    override fun onResume() {
        super.onResume()
        LifecycleManager.notify(this, LifeEvent.SHOW)
    }

    fun startActivity(activityClazz: Class<*>) {
        val intent = Intent(this, activityClazz)
        startActivity(intent)
    }
}
