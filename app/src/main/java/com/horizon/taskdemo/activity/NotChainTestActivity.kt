package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.horizon.task.UITask
import com.horizon.task.base.Priority
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_chain_test.*

// 此类用于与 ChainTestActivity对比
class NotChainTestActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chain_test)

        NotChainTask()
                .priority(Priority.IMMEDIATE)
                .host(this)
                .execute("hello")
    }

    private inner class NotChainTask : UITask<String, Integer, String>() {
        override fun generateTag(): String {
            // 一般情况下不需要重写这个函数，这里只是为了演示
            return "custom tag"
        }

        override fun onPreExecute() {
            result_tv.text = "running"
        }

        override fun doInBackground(vararg params: String): String? {
            for (i in 0..100 step 2) {
                Thread.sleep(10)
                publishProgress(Integer(i))
            }
            return "result is：" + params[0].toUpperCase()
        }

        override fun onProgressUpdate(vararg values: Integer) {
            val progress = values[0]
            progress_bar.progress = progress.toInt()
            progress_tv.text = "$progress%"
        }

        override fun onPostExecute(result: String?) {
            result_tv.text = result
        }

        override fun onCancelled() {
            showTips("ChainTask cancel ")
        }
    }


}