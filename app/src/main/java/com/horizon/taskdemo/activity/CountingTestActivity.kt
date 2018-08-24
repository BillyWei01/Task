package com.horizon.taskdemo.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.horizon.task.IOTask
import com.horizon.task.base.Priority
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

    private inner class CountingTask : IOTask<Integer, String, String>(){
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
