package com.horizon.taskdemo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.horizon.task.ChainTask
import com.horizon.task.base.Priority
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_chain_test.*

class ChainTestActivity : BaseActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chain_test)

        // 可在构造函数传入其他的TaskExecutor
        //val task = ChainTask<Double, Int, String>(TaskCenter.laneCP)

        // 使用ChainTask，并且executor是LaneExecutor的话，最好设置tag, 否则讲退化为PipeExecutor。
        val task = ChainTask<Double, Int, String>()
        task.tag("ChainTest")
                .preExecute { result_tv.text = "running" }
                .background { params ->
                    for (i in 0..100 step 2) {
                        Thread.sleep(10)
                        task.publishProgress(i)
                    }
                    "result is：" + (params[0] * 100)
                }
                .progressUpdate { values ->
                    val progress = values[0]
                    progress_bar.progress = progress
                    progress_tv.text = "$progress%"
                }
                .postExecute { result_tv.text = it }
                .cancel { showTips("ChainTask cancel ") }
                .priority(Priority.IMMEDIATE)
                .host(this)
                .execute(3.14)
    }

}