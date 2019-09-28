package com.horizon.task

import com.horizon.task.executor.TaskExecutor

/**
 * 拓展[UITask]，支持链式调用。
 *
 * 可以在构造函数指定要使用的 Executor。
 */
class ChainTask<Params, Progress, Result>(override val executor: TaskExecutor = TaskCenter.laneIO)
    : UITask<Params, Progress, Result>() {

    private var tag: String? = null

    private var mPreExecute: (() -> Unit)? = null
    private var mBackground: ((params: List<Params>) -> Result?)? = null
    private var mPostExecute: ((result: Result?) -> Unit)? = null
    private var mProgressUpdate: ((values: List<Progress>) -> Unit)? = null
    private var mCancelWithResult: ((result: Result?) -> Unit)? = null
    private var mCancel: (() -> Unit)? = null

    /**
     * 若设置了tag, 并且 [executor] 是 LaneExecutor 的话，会有 LaneExecutor 的特性；
     * 否则为 PipeExecutor 的特性。
     */
    fun tag(tag: String): ChainTask<Params, Progress, Result> {
        this.tag = tag
        return this
    }

    fun preExecute(preExecute: () -> Unit): ChainTask<Params, Progress, Result> {
        mPreExecute = preExecute
        return this
    }

    fun background(background: (params: List<Params>) -> Result?): ChainTask<Params, Progress, Result> {
        mBackground = background
        return this
    }

    fun postExecute(postExecute: (result: Result?) -> Unit): ChainTask<Params, Progress, Result> {
        mPostExecute = postExecute
        return this
    }

    fun progressUpdate(progressUpdate: (values: List<Progress>) -> Unit): ChainTask<Params, Progress, Result> {
        mProgressUpdate = progressUpdate
        return this
    }

    fun cancelWithResult(cancelWithResult: (result: Result?) -> Unit): ChainTask<Params, Progress, Result> {
        mCancelWithResult = cancelWithResult
        return this
    }

    fun cancel(cancel: () -> Unit): ChainTask<Params, Progress, Result> {
        mCancel = cancel
        return this
    }

    override fun generateTag(): String {
        return tag ?: ""
    }

    override fun onPreExecute() {
        mPreExecute?.invoke()
    }

    override fun doInBackground(vararg params: Params): Result? {
        return mBackground?.invoke(listOf(*params))
    }

    override fun onPostExecute(result: Result?) {
        mPostExecute?.invoke(result)
    }

    override fun onProgressUpdate(vararg values: Progress) {
        mProgressUpdate?.invoke(listOf(*values))
    }

    override fun onCancelled(result: Result?) {
        mCancelWithResult?.invoke(result)
        onCancelled()
    }

    override fun onCancelled() {
        mCancel?.invoke()
    }
}