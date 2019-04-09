package com.horizon.taskdemo.base

import android.app.Fragment
import com.horizon.task.lifecycle.LifeEvent
import com.horizon.task.lifecycle.LifecycleManager


abstract class BaseFragment : Fragment() {
    override fun onDestroy() {
        super.onDestroy()
        LifecycleManager.notify(this, LifeEvent.DESTROY)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        LifecycleManager.notify(this, if (isVisibleToUser) LifeEvent.SHOW else LifeEvent.HIDE)
    }
}