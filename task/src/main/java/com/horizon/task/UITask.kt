/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2018 Horizon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.horizon.task

import android.os.Handler
import android.os.Looper
import android.os.Process
import android.support.annotation.MainThread
import android.text.TextUtils
import android.util.Log
import com.horizon.task.base.LogProxy
import com.horizon.task.base.Priority
import com.horizon.task.executor.TaskExecutor
import com.horizon.task.lifecycle.Event
import com.horizon.task.lifecycle.LifecycleManager
import com.horizon.task.lifecycle.Listener
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * base on [android.os.AsyncTask], with some extend.
 * 1. Support priority
 * 2. Cancel task when Activity/Fragment destroy（need to call [host])
 * 3. Auto change priority when Activity/Fragment switch visible/invisible.
 * 4. More control abilities with support of [TaskExecutor]
 */
abstract class UITask<Params, Progress, Result> : Listener {
    private val mWorker: WorkerRunnable<Params, Result>
    private val mFuture: FutureTask<Result>

    @Volatile
    var isDone = false
        private set
    @Volatile
    var status = Status.PENDING
        private set
    private val mCancelled = AtomicBoolean()
    private val mTaskInvoked = AtomicBoolean()

    private val mFullName: String = this.javaClass.name
    protected val mSimpleName: String

    @Volatile
    private var mPriority = Priority.NORMAL
    private var mHostHash = 0
    private var mExecutionTime: Int = 0

    val isCancelled: Boolean
        get() = mCancelled.get()

    private val mTag: String by lazy { generateTag() }

    /**
     * Generally, tag a task with full name. <br></br>
     * Override this method and use some other info to identify the task if necessary.
     *
     * @return tag of task
     */
    protected open fun generateTag(): String {
        return mFullName
    }

    protected abstract val executor: TaskExecutor

    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }

    companion object Companion {
        private val HANDLER = Handler(Looper.getMainLooper())
    }

    init {
        val simpleName = this.javaClass.simpleName
        mSimpleName = if (TextUtils.isEmpty(simpleName)) {
            val index = mFullName.lastIndexOf('.') + 1
            if (index > 0) mFullName.substring(index) else mFullName
        } else {
            simpleName
        }

        mWorker = object : WorkerRunnable<Params, Result>() {
            @Throws(Exception::class)
            override fun call(): Result? {
                mTaskInvoked.set(true)

                var beginTime: Long = 0
                if (LogProxy.isDebug) {
                    beginTime = System.nanoTime()
                }
                var result: Result? = null
                try {
                    if (!isCancelled) {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                        result = doInBackground(*this.mParams)
                    }
                } catch (e: Throwable) {
                    mCancelled.set(true)
                    LogProxy.e(mSimpleName, e)
                }

                if (LogProxy.isDebug) {
                    mExecutionTime = ((System.nanoTime() - beginTime) / 1000000L).toInt()
                }
                postResult(result)
                return result
            }
        }

        mFuture = object : FutureTask<Result>(mWorker) {
            override fun done() {
                this@UITask.isDone = true
                try {
                    postResultIfNotInvoked(get())
                } catch (e: InterruptedException) {
                    Log.w(mSimpleName, e)
                } catch (e: ExecutionException) {
                    throw RuntimeException("An e occured while executing doInBackground()",
                            e.cause)
                } catch (e: CancellationException) {
                    postResultIfNotInvoked(null)
                }
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                this@UITask.mCancelled.set(true)
                return super.cancel(mayInterruptIfRunning)
            }
        }
    }

    internal abstract class WorkerRunnable<Params, Result> : Callable<Result> {
        internal lateinit var mParams: Array<out Params>
    }

    private fun postResultIfNotInvoked(result: Result?) {
        if(mFuture.isCancelled){
            mCancelled.set(true)
        }
        val wasTaskInvoked = mTaskInvoked.get()
        if (!wasTaskInvoked) {
            postResult(result)
        }
    }

    private fun postResult(result: Result?) {
        HANDLER.post { finish(result) }
    }

    @SafeVarargs
    protected open fun publishProgress(vararg values: Progress) {
        if (!isCancelled) {
            HANDLER.post { onProgressUpdate(*values) }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (mPriority != Priority.IMMEDIATE) {
            executor.remove(mFuture, mPriority)
        }
        return mFuture.cancel(mayInterruptIfRunning)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun get(): Result {
        return mFuture.get()
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    operator fun get(timeout: Long, unit: TimeUnit): Result {
        return mFuture.get(timeout, unit)
    }

    fun execute(vararg params: Params) {
        if (status != Status.PENDING) {
            return
        }

        status = Status.RUNNING
        onPreExecute()
        mWorker.mParams = params
        executor.execute(mFuture, mTag, mPriority)
    }

    private fun finish(result: Result?) {
        onTaskFinish()
        detachHost()
        if (isCancelled) {
            logResult("cancel")
            onCancelled(result)
        } else {
            logResult("finish")
            onPostExecute(result)
        }
        status = Status.FINISHED
    }

    private fun logResult(result: String) {
        if (LogProxy.isDebug) {
            val sb = StringBuilder(64)
            sb.append("task ").append(result).append("  ")
            if (mTaskInvoked.get()) {
                sb.append("execute:").append(mExecutionTime).append("ms")
            }
            Log.d(mSimpleName, sb.toString())
        }
    }

    @MainThread
    protected open fun onPreExecute() {
    }

    // WorkerThread
    protected abstract fun doInBackground(vararg params: Params): Result?

    @MainThread
    protected open fun onPostExecute(result: Result?) {
    }

    @MainThread
    protected open fun onProgressUpdate(vararg values: Progress) {
    }

    @MainThread
    protected open fun onCancelled(result: Result?) {
        onCancelled()
    }

    @MainThread
    protected open fun onCancelled() {
    }

    /**
     * call after doInBackground,  before onPostExecute or onCancelled
     */
    @MainThread
    protected open fun onTaskFinish() {
    }

    /**
     * @param priority of task, default is [Priority.NORMAL]. <br></br>
     * @return task itself
     * @see Priority
     */
    fun priority(priority: Int): UITask<Params, Progress, Result> {
        var p = priority
        if (priority != Priority.IMMEDIATE) {
            if (priority > Priority.HIGH) {
                p = Priority.HIGH
            } else if (priority < Priority.LOW) {
                p = Priority.LOW
            }
        }
        mPriority = p
        return this
    }

    private fun changePriority(increment: Int) {
        if (mPriority != Priority.IMMEDIATE) {
            mPriority = executor.changePriority(mFuture, mPriority, increment)
        }
    }

    /**
     * set task's host
     *
     * @param host may be one of Activity, Fragment or Dialog
     * @see LifecycleManager.register
     */
    fun host(host: Any): UITask<Params, Progress, Result> {
        return hostHash(System.identityHashCode(host))
    }

    fun hostHash(hostHash: Int): UITask<Params, Progress, Result> {
        this.mHostHash = hostHash
        LifecycleManager.register(hostHash, this)
        return this
    }

    private fun detachHost() {
        if (mHostHash != 0) {
            LifecycleManager.unregister(mHostHash, this)
        }
    }

    override fun onEvent(event: Int) {
        if (event == Event.DESTROY) {
            if (!isCancelled && status != Status.FINISHED) {
                // no need to call detachHost for host destroy
                mHostHash = 0
                cancel(true)
            }
        } else if (event == Event.SHOW) {
            changePriority(+1)
        } else if (event == Event.HIDE) {
            changePriority(-1)
        }
    }
}