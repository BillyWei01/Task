package com.horizon.taskdemo.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.horizon.task.TaskCenter
import com.horizon.task.UITask
import com.horizon.task.base.Priority
import com.horizon.task.executor.TaskExecutor
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity


class CountingTestActivity : BaseActivity() {
    private lateinit var mCountingTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counting_test)

        mCountingTv = findViewById(R.id.counting_tv)

        CountingTask()
                .host(this)
                .priority(Priority.IMMEDIATE)
                .execute(20 as Integer)
    }

    private inner class CountingTask : UITask<Integer, String, String>(){
        // 事实上这里不需要 TaskCenter.laneCP, 只是为了展示使用方法
        override val executor: TaskExecutor
            get() = TaskCenter.laneCP

        override fun doInBackground(vararg params: Integer): String? {
            val n = params[0] as Int
            return try {
                for (count in n downTo 1) {
                    publishProgress(Integer.toString(count))
                    Thread.sleep(1000)
                }
                "done"
            } catch (e: InterruptedException) {
                Log.w(tag, e.javaClass.simpleName + " occurred")
                "cancel"
            }
        }

        override fun onProgressUpdate(vararg values: String) {
            mCountingTv.text = values[0]
        }

        override fun onCancelled(result: String?) {
            Log.i(tag, "task result:  " + result!!)
        }

        override fun onPostExecute(result: String?) {
            mCountingTv.text = result
        }
    }
}
