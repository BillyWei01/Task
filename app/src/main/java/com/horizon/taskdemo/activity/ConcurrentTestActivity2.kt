package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.horizon.task.UITask
import com.horizon.task.executor.LaneExecutor
import com.horizon.task.executor.PipeExecutor
import com.horizon.task.executor.TaskExecutor
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// 这个用例有点复杂, ConcurrentTestActivity会好理解一些
class ConcurrentTestActivity2 : BaseActivity() {
    companion object {
        private const val CONCURRENT_SIZE = 4
    }

    private val mCustomExecutor = LaneExecutor(PipeExecutor(CONCURRENT_SIZE), true)

    private var mCount = 0
    internal lateinit var mProgressTv: TextView
    internal lateinit var mStateTv: TextView
    internal lateinit var mResultTv: TextView

    // 32个任务同时启动，8个可以马上执行，8个进入等待，16个被丢弃
    internal val mTagCount = 8
    internal val mExpectedCount = mTagCount * 2
    private val mTaskCount = mTagCount * 4

    internal val mSleepTime = 200
    private val mWaitTime = 1000L
    internal val mExpectedTime = mExpectedCount * mSleepTime / CONCURRENT_SIZE + mWaitTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concurrent_test2)

        mStateTv = findViewById(R.id.state_tv)
        mProgressTv = findViewById(R.id.progess_tv)
        mResultTv = findViewById(R.id.result_tv)

        object : UITask<Void, Void, String>() {
            private var actuallyTime: Long = 0

            override fun doInBackground(vararg params: Void): String? {
                val start = System.nanoTime()
                startTest()
                val end = System.nanoTime()
                actuallyTime = (end - start) / 1000000
                return null
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(result: String?) {
                mStateTv.text = "done"

                // Thread.sleep(long) 通常会多sleep()一点时间
                // 所以 mExpectedTim  一般预计的多一点
                // 但是mCount是准的，和mExpectedCount一样多
                mResultTv.text = ("use time:" + actuallyTime
                        + "\nexpectedTime:" + mExpectedTime
                        + "\nfinish count:" + mCount
                        + "\nexpectedCount: " + mExpectedCount)
            }
        }
                .host(this)
                .execute()

    }

    private fun startTest() {
        val latch = CountDownLatch(mExpectedCount)

        for (i in 0 until mTaskCount) {
            // 如果 onPreExecute()不做UI操作
            // 在后台线程中启动UITask也是没有问题的
            object : UITask<Void, Void, Void>() {
                override val executor: TaskExecutor
                    get() = mCustomExecutor

                override fun generateTag(): String {
                    // 给任务打标签
                    return "TASK" + i % mTagCount
                }

                override fun doInBackground(vararg params: Void): Void? {
                    try {
                        Thread.sleep(mSleepTime.toLong())
                    } catch (e: InterruptedException) {
                        Log.w(tag, e.javaClass.simpleName + " occurred")
                    } finally {
                        latch.countDown()
                    }
                    return null
                }

                @SuppressLint("SetTextI18n")
                override fun onPostExecute(result: Void?) {
                    mProgressTv.text = (++mCount).toString() + " task finish"
                }
            }.host(this).execute()
        }

        try {
            latch.await((mExpectedTime + 3000), TimeUnit.MILLISECONDS)

            Thread.sleep(mWaitTime)
        } catch (e: InterruptedException) {
            Log.e(tag, e.message, e)
        }

    }



}
