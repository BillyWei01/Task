package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.horizon.task.TaskCenter
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_concurrent_test.*
import java.util.concurrent.atomic.AtomicInteger


class ConcurrentTestActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concurrent_test)

        val a = AtomicInteger()
        val b = AtomicInteger()
        val c = AtomicInteger()

        // TaskCenter.io 没做任务去重，所以a=5
        for (i in 1..5) {
            TaskCenter.computation.execute {
                Thread.sleep(100)
                Log.d(tag, "TaskCenter.io")
                a.incrementAndGet()
            }
        }

        // TaskCenter.serial 不会忽略任务（但会串行执行），所以b=5
        for (i in 1..5) {
            TaskCenter.serial.execute ("serial", {
                Thread.sleep(100)
                Log.d(tag, "TaskCenter.serial")
                b.incrementAndGet()
            })
        }

        // TaskCenter.laneIO 会只保留一个在等待的任务，后来者会被忽略，所以c=2
        for (i in 1..5) {
            TaskCenter.laneIO.execute("laneIO", {
                Thread.sleep(100)
                Log.d(tag, "TaskCenter.laneCP $i")
                c.incrementAndGet()
            })
        }

        // 观察log, 将会看到:
        // TaskCenter.io 几乎同时打印
        // TaskCenter.serial 每隔100ms打印一条
        // TaskCenter.laneIO 也是每隔100ms打印一条,但只会打印2条，
        //                   因为在几乎同一时刻提交了5个任务，被过滤了3个

        state_tv.postDelayed({
            state_tv.text = "a:$a  b:$b  c:$c"
        }, 1000)
    }


}
