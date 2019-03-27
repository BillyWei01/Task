package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import com.horizon.task.ChainTask
import com.horizon.task.TaskCenter
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_chain_test.*

class ChainTestActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chain_test)

        //val task = ChainTask<Double, Int, String>(TaskCenter.laneCP)
        val task = ChainTask<Double, Int, String>()
        task.tag("ChainTest")
                .preExecute { result_tv.text = "running" }
                .background { params ->
                    for (i in 0..100 step 2) {
                        // 讲道理这里应该 catch InterruptedException
                        Thread.sleep(10)
                        task.publishProgress(i)
                    }
                    "result is：" + (params[0] * 100)
                }
                .progressUpdate {
                    val progress = it[0]
                    progress_bar.progress = progress
                    progress_tv.text = "$progress%"
                }
                .cancel { showTips("ChainTask cancel ") }
                .postExecute { result_tv.text = it }
                .host(this)
                .execute(3.14)
    }

}