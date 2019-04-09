package com.horizon.taskdemo.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.horizon.task.executor.PipeExecutor
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity


class SerialTestActivity : BaseActivity() {
    private var mCountingTv: TextView? = null
    private var destroy = false
    private var count = 0
    private var createTime: Long = 0

    private val mSerialExecutor = PipeExecutor(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serial_test)

        mCountingTv = findViewById(R.id.counting_tv)
        createTime = System.nanoTime()
        for (i in 0 until N) {
            // 两种方式实现串行：TaskCenter.serial更方便一些；PipeExecutor(1)更独立一些
            // TaskCenter.serial.execute("CountingTask", CountingTask())
            mSerialExecutor.execute(CountingTask())
        }
    }

    private inner class CountingTask : Runnable {
        override fun run() {
            count++
            Log.d(tag, "task count: $count")

            if (!destroy) {
                try {
                    Thread.sleep(1000L)
                } catch (ignore: InterruptedException) {
                }

                val t = (System.nanoTime() - createTime) / 1000000
                val text = "$count task finished, \nuse time: $t"
                runOnUiThread { mCountingTv!!.text = text }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy = true
    }

    companion object {
        private const val N = 5
    }
}
