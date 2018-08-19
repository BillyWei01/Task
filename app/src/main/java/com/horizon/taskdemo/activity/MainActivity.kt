package com.horizon.taskdemo.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import com.horizon.taskdemo.base.TaskSchedulers
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

class MainActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.loading_btn).setOnClickListener(this)
        findViewById<View>(R.id.counting_btn).setOnClickListener(this)
        findViewById<View>(R.id.serial_btn).setOnClickListener(this)
        findViewById<View>(R.id.concurrent_btn).setOnClickListener(this)

        Observable.range(1, 8)
                .flatMap { i ->
                    Observable.just(i).subscribeOn(TaskSchedulers.computation)
                            .map { Thread.sleep(1050); it * 10 }
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe { number ->
                    Log.d(tag, "number:" + number!!)
                }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loading_btn -> startActivity(LoadingTestActivity::class.java)
            R.id.counting_btn -> startActivity(CountingTestActivity::class.java)
            R.id.serial_btn -> startActivity(SerialTestActivity::class.java)
            R.id.concurrent_btn -> startActivity(ConcurrentTestActivity::class.java)
        }
    }

}

