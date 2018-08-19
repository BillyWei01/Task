package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.horizon.task.CPTask
import com.horizon.task.TaskCenter
import com.horizon.task.executor.LaneExecutor
import com.horizon.task.executor.PipeExecutor
import com.horizon.task.executor.TaskExecutor
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class ConcurrentTestActivity : BaseActivity() {
    private val mCustomExecutor = LaneExecutor(PipeExecutor(CONCURRENT_SIZE), true)

    private var mCount = 0
    internal lateinit var mProgressTv: TextView
    internal lateinit var mStateTv: TextView
    internal lateinit var mResultTv: TextView

    internal val mSleepTime = 200
    private val mWaitTime = 1000L
    internal val mTagCount = 16
    internal val mExpectedCount = mTagCount * 2 // scheduled and waiting
    internal val mExpectedTime = mExpectedCount * mSleepTime / CONCURRENT_SIZE + mWaitTime
    private val mTaskCount = mExpectedCount * 2 // finally half of task would be ignored

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concurrent_test)

        mStateTv = findViewById(R.id.state_tv)
        mProgressTv = findViewById(R.id.progess_tv)
        mResultTv = findViewById(R.id.result_tv)

        TaskCenter.io.execute{
            // do something
        }

        object : CPTask<Void, Void, String>() {
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

                // Thread.sleep() generally use more time than expected,
                // so mExpectedTime generally more than actually time,
                // but mCount expected equal to mExpectedCount
                mResultTv.text = ("use time:" + actuallyTime
                        + "\nexpectedTime:" + mExpectedTime
                        + "\nfinish count:" + mCount
                        + "\nexpectedCount: " + mExpectedCount)
            }
        }.setHost(this).execute()

    }

    private fun startTest() {
        val latch = CountDownLatch(mExpectedCount)

        for (i in 0 until mTaskCount) {
            // if onPreExecute() do nothing about UI,
            // it's ok to execute task in background thread
            object : CPTask<Void, Void, Void>() {
                override val executor: TaskExecutor
                    get() = mCustomExecutor

                override fun generateTag(): String {
                    // tag the task
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
            }.setHost(this).execute()
        }

        try {
            latch.await((mExpectedTime + 3000), TimeUnit.MILLISECONDS)

            Thread.sleep(mWaitTime)
        } catch (e: InterruptedException) {
            Log.e(tag, e.message, e)
        }

    }

    companion object {
        private const val CONCURRENT_SIZE = 4
    }


}
